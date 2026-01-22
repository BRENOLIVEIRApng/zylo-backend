-- ORDENS DE SERVIÇO
CREATE TABLE ordens_servico (
                                codigo_os BIGSERIAL PRIMARY KEY,
                                numero_os VARCHAR(20) UNIQUE NOT NULL,  -- OS-2025-000001
                                codigo_cliente BIGINT NOT NULL,
                                codigo_contrato BIGINT NOT NULL,
                                codigo_servico BIGINT NOT NULL,
                                titulo_os VARCHAR(200) NOT NULL,
                                descricao_os TEXT,
                                prioridade VARCHAR(20) DEFAULT 'NORMAL' NOT NULL,
                                status_os VARCHAR(30) DEFAULT 'ABERTA' NOT NULL,
                                codigo_responsavel BIGINT,
                                sla_horas INTEGER,
                                data_abertura TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                data_limite TIMESTAMP WITH TIME ZONE,
                                data_finalizacao TIMESTAMP WITH TIME ZONE,
                                criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                criado_por BIGINT NOT NULL,

                                CONSTRAINT fk_os_cliente FOREIGN KEY (codigo_cliente) REFERENCES clientes(codigo_cliente) ON DELETE RESTRICT,
                                CONSTRAINT fk_os_contrato FOREIGN KEY (codigo_contrato) REFERENCES contratos(codigo_contrato) ON DELETE RESTRICT,
                                CONSTRAINT fk_os_servico FOREIGN KEY (codigo_servico) REFERENCES servicos(codigo_servico) ON DELETE RESTRICT,
                                CONSTRAINT fk_os_responsavel FOREIGN KEY (codigo_responsavel) REFERENCES usuarios(codigo_usuario) ON DELETE SET NULL,
                                CONSTRAINT fk_os_criado_por FOREIGN KEY (criado_por) REFERENCES usuarios(codigo_usuario) ON DELETE RESTRICT,

                                CONSTRAINT chk_os_prioridade CHECK (prioridade IN ('BAIXA', 'NORMAL', 'ALTA', 'URGENTE')),
                                CONSTRAINT chk_os_status CHECK (status_os IN ('ABERTA', 'EM_ANDAMENTO', 'AGUARDANDO_CLIENTE', 'FINALIZADA', 'CANCELADA')),
                                CONSTRAINT chk_os_datas CHECK (data_finalizacao IS NULL OR data_finalizacao >= data_abertura)
);

-- ÍNDICES ESTRATÉGICOS
CREATE UNIQUE INDEX idx_os_numero ON ordens_servico(numero_os);
CREATE INDEX idx_os_cliente ON ordens_servico(codigo_cliente);
CREATE INDEX idx_os_contrato ON ordens_servico(codigo_contrato);
CREATE INDEX idx_os_responsavel ON ordens_servico(codigo_responsavel) WHERE codigo_responsavel IS NOT NULL;
CREATE INDEX idx_os_status ON ordens_servico(status_os);
CREATE INDEX idx_os_prioridade ON ordens_servico(prioridade);
CREATE INDEX idx_os_data_abertura ON ordens_servico(data_abertura DESC);
CREATE INDEX idx_os_data_limite ON ordens_servico(data_limite) WHERE data_limite IS NOT NULL;

-- Índice para Kanban (status + prioridade + data)
CREATE INDEX idx_os_kanban ON ordens_servico(status_os, prioridade, data_abertura DESC);

-- Índice para OS críticas (SLA próximo)
CREATE INDEX idx_os_sla_critico ON ordens_servico(data_limite)
    WHERE status_os IN ('ABERTA', 'EM_ANDAMENTO') AND data_limite IS NOT NULL;

-- Índice para "Minhas OS" (por responsável)
CREATE INDEX idx_os_responsavel_status ON ordens_servico(codigo_responsavel, status_os);

-- COMENTÁRIOS DO OS
CREATE TABLE comentarios_os (
                                codigo_comentario BIGSERIAL PRIMARY KEY,
                                codigo_os BIGINT NOT NULL,
                                codigo_usuario BIGINT NOT NULL,
                                comentario TEXT NOT NULL,
                                interno BOOLEAN DEFAULT TRUE NOT NULL,
                                data_hora_comentario TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

                                CONSTRAINT fk_comentarios_os FOREIGN KEY (codigo_os) REFERENCES ordens_servico(codigo_os) ON DELETE CASCADE,
                                CONSTRAINT fk_comentarios_usuario FOREIGN KEY (codigo_usuario) REFERENCES usuarios(codigo_usuario) ON DELETE RESTRICT,
                                CONSTRAINT chk_comentario_minimo CHECK (LENGTH(TRIM(comentario)) >= 3)
);

CREATE INDEX idx_comentarios_os ON comentarios_os(codigo_os, data_hora_comentario DESC);
CREATE INDEX idx_comentarios_usuario ON comentarios_os(codigo_usuario);

-- HISTÓRICO DE STATUS DA OS
CREATE TABLE historico_os_status (
                                     codigo_historico_status BIGSERIAL PRIMARY KEY,
                                     codigo_os BIGINT NOT NULL,
                                     status_anterior VARCHAR(30),
                                     status_novo VARCHAR(30) NOT NULL,
                                     codigo_usuario BIGINT,
                                     data_hora_mudanca TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                     observacao_mudanca TEXT,

                                     CONSTRAINT fk_historico_os FOREIGN KEY (codigo_os) REFERENCES ordens_servico(codigo_os) ON DELETE CASCADE,
                                     CONSTRAINT fk_historico_usuario FOREIGN KEY (codigo_usuario) REFERENCES usuarios(codigo_usuario) ON DELETE SET NULL
);

CREATE INDEX idx_historico_os_os ON historico_os_status(codigo_os, data_hora_mudanca DESC);

-- TRIGGER: Gerar número da OS
CREATE OR REPLACE FUNCTION gerar_numero_os()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.numero_os IS NULL OR NEW.numero_os = '' THEN
        NEW.numero_os := gerar_numero_sequencial('OS', 'seq_numero_os', 6);
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_os_gerar_numero
    BEFORE INSERT ON ordens_servico
    FOR EACH ROW
    EXECUTE FUNCTION gerar_numero_os();

-- TRIGGER: Calcular data limite com SLA
CREATE OR REPLACE FUNCTION calcular_data_limite_os()
RETURNS TRIGGER AS $$
DECLARE
sla_contrato INTEGER;
    sla_servico INTEGER;
    sla_final INTEGER;
BEGIN
    -- Se data_limite já foi definida manualmente, não calcula
    IF NEW.data_limite IS NOT NULL THEN
        RETURN NEW;
END IF;

    -- Pega SLA do contrato
SELECT sla_horas INTO sla_contrato FROM contratos WHERE codigo_contrato = NEW.codigo_contrato;

-- Pega SLA do serviço
SELECT sla_horas INTO sla_servico FROM servicos WHERE codigo_servico = NEW.codigo_servico;

-- Prioridade: SLA da OS > SLA do Contrato > SLA do Serviço
sla_final := COALESCE(NEW.sla_horas, sla_contrato, sla_servico, 24);

    -- Calcula data limite
    NEW.data_limite := calcular_data_limite_sla(NEW.data_abertura, sla_final);

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_os_calcular_data_limite
    BEFORE INSERT ON ordens_servico
    FOR EACH ROW
    EXECUTE FUNCTION calcular_data_limite_os();

-- TRIGGER: Registrar mudança de status
CREATE OR REPLACE FUNCTION registrar_mudanca_status_os()
RETURNS TRIGGER AS $$
DECLARE
codigo_usuario_atual BIGINT;
BEGIN
BEGIN
        codigo_usuario_atual := CURRENT_SETTING('app.codigo_usuario', TRUE)::BIGINT;
EXCEPTION WHEN OTHERS THEN
        codigo_usuario_atual := NULL;
END;

    IF (TG_OP = 'UPDATE' AND OLD.status_os != NEW.status_os) THEN
        INSERT INTO historico_os_status (codigo_os, status_anterior, status_novo, codigo_usuario)
        VALUES (NEW.codigo_os, OLD.status_os, NEW.status_os, codigo_usuario_atual);

        -- Se finalizou, registra data
        IF NEW.status_os = 'FINALIZADA' AND NEW.data_finalizacao IS NULL THEN
            NEW.data_finalizacao := CURRENT_TIMESTAMP;
END IF;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_os_registrar_mudanca_status
    BEFORE UPDATE ON ordens_servico
    FOR EACH ROW
    EXECUTE FUNCTION registrar_mudanca_status_os();