-- =====================================================
-- V004__criar_tabelas_servicos_contratos.sql
-- Descrição: Catálogo de serviços e gestão de contratos
-- Autor: ERP Portal Team
-- Data: 2025-01-10
-- =====================================================

-- =====================================================
-- TABELA: Serviços (Catálogo)
-- =====================================================
CREATE TABLE servicos (
                          codigo_servico          BIGSERIAL PRIMARY KEY,

    -- Identificação
                          nome_servico            VARCHAR(200) NOT NULL,
                          descricao_servico       TEXT,

    -- Precificação
                          tipo_cobranca           VARCHAR(30) NOT NULL,
                          valor_base              NUMERIC(10,2) NOT NULL,  -- Valor padrão (pode ser alterado no contrato)

    -- SLA
                          sla_horas               INTEGER,  -- SLA padrão em horas

    -- Categorização
                          categoria               VARCHAR(50),

    -- Status
                          ativo                   BOOLEAN DEFAULT TRUE NOT NULL,

    -- Auditoria
                          criado_em               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          atualizado_em           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Constraints
                          CONSTRAINT chk_servico_tipo_cobranca CHECK (
                              tipo_cobranca IN ('MENSALIDADE', 'PROJETO_FECHADO', 'HORAS_CONTRATADAS', 'SOB_DEMANDA')
                              ),

                          CONSTRAINT chk_servico_categoria CHECK (
                              categoria IS NULL OR categoria IN ('DESENVOLVIMENTO', 'SUPORTE', 'CONSULTORIA', 'INFRAESTRUTURA')
                              ),

                          CONSTRAINT chk_servico_valor_positivo CHECK (valor_base >= 0),

                          CONSTRAINT chk_servico_sla_positivo CHECK (sla_horas IS NULL OR sla_horas > 0),

                          CONSTRAINT chk_servico_nome_minimo CHECK (LENGTH(TRIM(nome_servico)) >= 3)
);

-- =====================================================
-- ÍNDICES - Serviços
-- =====================================================
CREATE INDEX idx_servicos_ativo ON servicos(ativo) WHERE ativo = TRUE;
CREATE INDEX idx_servicos_categoria ON servicos(categoria);
CREATE INDEX idx_servicos_tipo_cobranca ON servicos(tipo_cobranca);
CREATE INDEX idx_servicos_nome_lower ON servicos(LOWER(nome_servico));

-- Índice para listagem padrão (ativos ordenados por nome)
CREATE INDEX idx_servicos_listagem ON servicos(ativo, nome_servico) WHERE ativo = TRUE;

COMMENT ON TABLE servicos IS
'Catálogo de serviços oferecidos pela software house';

COMMENT ON COLUMN servicos.tipo_cobranca IS
'MENSALIDADE: recorrente mensal | PROJETO_FECHADO: valor fixo | HORAS_CONTRATADAS: por hora | SOB_DEMANDA: não recorrente';

COMMENT ON COLUMN servicos.valor_base IS
'Valor padrão do serviço. Pode ser customizado no contrato.';

-- =====================================================
-- TABELA: Contratos
-- =====================================================
CREATE TABLE contratos (
                           codigo_contrato         BIGSERIAL PRIMARY KEY,

    -- Identificação
                           numero_contrato         VARCHAR(20) UNIQUE NOT NULL,  -- Gerado: CT-2025-000001
                           codigo_cliente          BIGINT NOT NULL,

    -- Tipo e Valor
                           tipo_contrato           VARCHAR(30) NOT NULL,
                           valor_total             NUMERIC(10,2) NOT NULL,

    -- Vigência
                           data_inicio             DATE NOT NULL,
                           data_fim                DATE NOT NULL,
                           duracao_meses           INTEGER,  -- Para contratos mensais

    -- SLA
                           sla_horas               INTEGER,  -- Sobrescreve SLA dos serviços se definido

    -- Status
                           status_contrato         VARCHAR(20) DEFAULT 'ATIVO' NOT NULL,

    -- Observações
                           observacoes             TEXT,

    -- Auditoria Completa
                           criado_em               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                           criado_por              BIGINT NOT NULL,
                           atualizado_em           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                           atualizado_por          BIGINT NOT NULL,
                           excluido_em             TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                           excluido_por            BIGINT NOT NULL,

    -- Foreign Keys
                           CONSTRAINT fk_contratos_cliente
                               FOREIGN KEY (codigo_cliente)
                                   REFERENCES clientes(codigo_cliente)
                                   ON DELETE RESTRICT,  -- Não permite excluir cliente com contratos

                           CONSTRAINT fk_contratos_criado_por
                               FOREIGN KEY (criado_por)
                                   REFERENCES usuarios(codigo_usuario)
                                   ON DELETE RESTRICT,

                           CONSTRAINT fk_contratos_atualizado_por
                               FOREIGN KEY (atualizado_por)
                                   REFERENCES usuarios(codigo_usuario)
                                   ON DELETE RESTRICT,

    -- Constraints de Validação
                           CONSTRAINT chk_contrato_tipo CHECK (
                               tipo_contrato IN ('MENSALIDADE', 'PROJETO_FECHADO', 'HORAS_CONTRATADAS')
                               ),

                           CONSTRAINT chk_contrato_status CHECK (
                               status_contrato IN ('ATIVO', 'SUSPENSO', 'ENCERRADO', 'CANCELADO')
                               ),

                           CONSTRAINT chk_contrato_valor_positivo CHECK (valor_total >= 0),

                           CONSTRAINT chk_contrato_datas CHECK (data_fim >= data_inicio),

                           CONSTRAINT chk_contrato_duracao_positiva CHECK (
                               duracao_meses IS NULL OR duracao_meses > 0
                               ),

                           CONSTRAINT chk_contrato_sla_positivo CHECK (
                               sla_horas IS NULL OR sla_horas > 0
                               )
);

-- =====================================================
-- ÍNDICES - Contratos
-- =====================================================
CREATE UNIQUE INDEX idx_contratos_numero ON contratos(numero_contrato);
CREATE INDEX idx_contratos_cliente ON contratos(codigo_cliente);
CREATE INDEX idx_contratos_status ON contratos(status_contrato);
CREATE INDEX idx_contratos_vigencia ON contratos(data_inicio, data_fim);
CREATE INDEX idx_contratos_tipo ON contratos(tipo_contrato);

-- Índice para contratos ativos de um cliente
CREATE INDEX idx_contratos_cliente_ativo
    ON contratos(codigo_cliente, status_contrato)
    WHERE status_contrato = 'ATIVO';

-- Índice para contratos que vencem em breve
CREATE INDEX idx_contratos_vencimento
    ON contratos(data_fim)
    WHERE status_contrato = 'ATIVO';

-- Índice para auditoria
CREATE INDEX idx_contratos_auditoria ON contratos(criado_por, atualizado_por);

COMMENT ON TABLE contratos IS
'Contratos firmados com clientes (mensalidades, projetos, horas contratadas)';

COMMENT ON COLUMN contratos.numero_contrato IS
'Número único do contrato no formato CT-2025-000001 (gerado automaticamente)';

COMMENT ON COLUMN contratos.status_contrato IS
'ATIVO: em vigência | SUSPENSO: pausado temporariamente | ENCERRADO: finalizado naturalmente | CANCELADO: encerrado antes do prazo';

-- =====================================================
-- TABELA: Contrato_Servicos (N:N com valores customizados)
-- =====================================================
CREATE TABLE contrato_servicos (
                                   codigo_contrato_servico BIGSERIAL PRIMARY KEY,
                                   codigo_contrato         BIGINT NOT NULL,
                                   codigo_servico          BIGINT NOT NULL,

    -- Valor customizado (pode ser diferente do valor_base do serviço)
                                   valor_servico           NUMERIC(10,2) NOT NULL,
                                   quantidade              INTEGER DEFAULT 1 NOT NULL,

    -- Foreign Keys
                                   CONSTRAINT fk_contrato_servicos_contrato
                                       FOREIGN KEY (codigo_contrato)
                                           REFERENCES contratos(codigo_contrato)
                                           ON DELETE CASCADE,  -- Se contrato é excluído, serviços vinculados também

                                   CONSTRAINT fk_contrato_servicos_servico
                                       FOREIGN KEY (codigo_servico)
                                           REFERENCES servicos(codigo_servico)
                                           ON DELETE RESTRICT,  -- Não permite excluir serviço vinculado a contrato

    -- Constraints
                                   CONSTRAINT uk_contrato_servico UNIQUE (codigo_contrato, codigo_servico),

                                   CONSTRAINT chk_contrato_servico_valor_positivo CHECK (valor_servico >= 0),

                                   CONSTRAINT chk_contrato_servico_quantidade_positiva CHECK (quantidade > 0)
);

-- =====================================================
-- ÍNDICES - Contrato Servicos
-- =====================================================
CREATE INDEX idx_contrato_servicos_contrato ON contrato_servicos(codigo_contrato);
CREATE INDEX idx_contrato_servicos_servico ON contrato_servicos(codigo_servico);

COMMENT ON TABLE contrato_servicos IS
'Serviços inclusos em cada contrato (com valores customizados por contrato)';

COMMENT ON COLUMN contrato_servicos.valor_servico IS
'Valor do serviço NESTE contrato (pode ser diferente do valor_base do catálogo)';

COMMENT ON COLUMN contrato_servicos.quantidade IS
'Quantidade de unidades (útil para "horas contratadas", "licenças", etc)';

-- =====================================================
-- TABELA: Histórico de Contratos (Auditoria)
-- =====================================================
CREATE TABLE historico_contratos (
                                     codigo_historico        BIGSERIAL PRIMARY KEY,
                                     codigo_contrato         BIGINT NOT NULL,

    -- Tipo de Alteração
                                     tipo_alteracao          VARCHAR(50) NOT NULL,
                                     descricao_alteracao     TEXT,

    -- Quem e Quando
                                     codigo_usuario          BIGINT,
                                     data_hora_alteracao     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign Keys
                                     CONSTRAINT fk_historico_contrato
                                         FOREIGN KEY (codigo_contrato)
                                             REFERENCES contratos(codigo_contrato)
                                             ON DELETE CASCADE,

                                     CONSTRAINT fk_historico_usuario
                                         FOREIGN KEY (codigo_usuario)
                                             REFERENCES usuarios(codigo_usuario)
                                             ON DELETE SET NULL,

    -- Constraints
                                     CONSTRAINT chk_historico_tipo_alteracao CHECK (
                                         tipo_alteracao IN (
                                                            'CRIACAO', 'EDICAO_VALOR', 'ADICAO_SERVICO', 'REMOCAO_SERVICO',
                                                            'SUSPENSAO', 'REATIVACAO', 'ENCERRAMENTO', 'CANCELAMENTO', 'RENOVACAO'
                                             )
                                         )
);

-- =====================================================
-- ÍNDICES - Histórico Contratos
-- =====================================================
CREATE INDEX idx_historico_contratos_contrato
    ON historico_contratos(codigo_contrato, data_hora_alteracao DESC);
CREATE INDEX idx_historico_contratos_usuario ON historico_contratos(codigo_usuario);
CREATE INDEX idx_historico_contratos_tipo ON historico_contratos(tipo_alteracao);

COMMENT ON TABLE historico_contratos IS
'Histórico de todas as alterações realizadas em contratos (rastreabilidade completa)';

-- =====================================================
-- TRIGGERS: Atualização automática de timestamps
-- =====================================================
CREATE TRIGGER trg_servicos_atualizar_timestamp
    BEFORE UPDATE ON servicos
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_timestamp_atualizacao();

CREATE TRIGGER trg_contratos_atualizar_timestamp
    BEFORE UPDATE ON contratos
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_timestamp_atualizacao();

-- =====================================================
-- TRIGGER: Gerar número do contrato automaticamente
-- =====================================================
CREATE OR REPLACE FUNCTION gerar_numero_contrato()
RETURNS TRIGGER AS $$
BEGIN
    -- Se número não foi fornecido, gera automaticamente
    IF NEW.numero_contrato IS NULL OR NEW.numero_contrato = '' THEN
        NEW.numero_contrato := gerar_numero_sequencial('CT', 'seq_numero_contrato', 6);
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_contratos_gerar_numero
    BEFORE INSERT ON contratos
    FOR EACH ROW
    EXECUTE FUNCTION gerar_numero_contrato();

COMMENT ON FUNCTION gerar_numero_contrato() IS
'Gera número do contrato automaticamente no formato CT-2025-000001';

-- =====================================================
-- TRIGGER: Recalcular valor total do contrato ao mudar serviços
-- =====================================================
CREATE OR REPLACE FUNCTION recalcular_valor_total_contrato()
RETURNS TRIGGER AS $$
DECLARE
novo_valor_total NUMERIC(10,2);
BEGIN
    -- Calcula o valor total baseado nos serviços vinculados
SELECT COALESCE(SUM(valor_servico * quantidade), 0)
INTO novo_valor_total
FROM contrato_servicos
WHERE codigo_contrato = COALESCE(NEW.codigo_contrato, OLD.codigo_contrato);

-- Atualiza o valor total do contrato
UPDATE contratos
SET valor_total = novo_valor_total,
    atualizado_em = CURRENT_TIMESTAMP
WHERE codigo_contrato = COALESCE(NEW.codigo_contrato, OLD.codigo_contrato);

RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_contrato_servicos_recalcular_valor_insert
    AFTER INSERT ON contrato_servicos
    FOR EACH ROW
    EXECUTE FUNCTION recalcular_valor_total_contrato();

CREATE TRIGGER trg_contrato_servicos_recalcular_valor_update
    AFTER UPDATE ON contrato_servicos
    FOR EACH ROW
    EXECUTE FUNCTION recalcular_valor_total_contrato();

CREATE TRIGGER trg_contrato_servicos_recalcular_valor_delete
    AFTER DELETE ON contrato_servicos
    FOR EACH ROW
    EXECUTE FUNCTION recalcular_valor_total_contrato();

COMMENT ON FUNCTION recalcular_valor_total_contrato() IS
'Recalcula automaticamente o valor_total do contrato quando serviços são adicionados/removidos/alterados';

-- =====================================================
-- TRIGGER: Registrar mudanças no histórico de contratos
-- =====================================================
CREATE OR REPLACE FUNCTION registrar_historico_contrato()
RETURNS TRIGGER AS $$
DECLARE
tipo_mudanca VARCHAR(50);
    descricao TEXT;
    codigo_usuario_atual BIGINT;
BEGIN
    -- Tenta pegar o código do usuário da sessão
BEGIN
        codigo_usuario_atual := CURRENT_SETTING('app.codigo_usuario', TRUE)::BIGINT;
EXCEPTION WHEN OTHERS THEN
        codigo_usuario_atual := NULL;
END;

    -- INSERT = Criação
    IF (TG_OP = 'INSERT') THEN
        tipo_mudanca := 'CRIACAO';
        descricao := 'Contrato criado com valor total de R$ ' || NEW.valor_total;

    -- UPDATE = Detectar tipo de mudança
    ELSIF (TG_OP = 'UPDATE') THEN
        -- Mudança de status
        IF OLD.status_contrato != NEW.status_contrato THEN
            CASE NEW.status_contrato
                WHEN 'SUSPENSO' THEN
                    tipo_mudanca := 'SUSPENSAO';
                    descricao := 'Contrato suspenso';
WHEN 'ATIVO' THEN
                    tipo_mudanca := 'REATIVACAO';
                    descricao := 'Contrato reativado';
WHEN 'ENCERRADO' THEN
                    tipo_mudanca := 'ENCERRAMENTO';
                    descricao := 'Contrato encerrado';
WHEN 'CANCELADO' THEN
                    tipo_mudanca := 'CANCELAMENTO';
                    descricao := 'Contrato cancelado';
END CASE;

        -- Mudança de valor
        ELSIF OLD.valor_total != NEW.valor_total THEN
            tipo_mudanca := 'EDICAO_VALOR';
            descricao := 'Valor alterado de R$ ' || OLD.valor_total || ' para R$ ' || NEW.valor_total;

        -- Outras mudanças
ELSE
            RETURN NEW;  -- Não registra mudanças não críticas
END IF;

ELSE
        RETURN NEW;
END IF;

    -- Insere no histórico
INSERT INTO historico_contratos (
    codigo_contrato,
    tipo_alteracao,
    descricao_alteracao,
    codigo_usuario
) VALUES (
             NEW.codigo_contrato,
             tipo_mudanca,
             descricao,
             codigo_usuario_atual
         );

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_contratos_historico
    AFTER INSERT OR UPDATE ON contratos
                        FOR EACH ROW
                        EXECUTE FUNCTION registrar_historico_contrato();

COMMENT ON FUNCTION registrar_historico_contrato() IS
'Registra automaticamente alterações críticas no histórico de contratos';

-- =====================================================
-- TRIGGER: Encerrar automaticamente contratos vencidos
-- =====================================================
-- Nota: Este trigger seria executado por um JOB diário, não em cada operação
-- Por questão de performance, isso será implementado no backend

-- =====================================================
-- VIEWS ÚTEIS
-- =====================================================

-- View: Contratos Ativos com Cliente e Valor
CREATE OR REPLACE VIEW vw_contratos_ativos AS
SELECT
    ct.codigo_contrato,
    ct.numero_contrato,
    cl.codigo_cliente,
    cl.razao_social,
    cl.nome_fantasia,
    ct.tipo_contrato,
    ct.valor_total,
    ct.data_inicio,
    ct.data_fim,
    ct.status_contrato,
    -- Dias até vencimento
    (ct.data_fim - CURRENT_DATE) AS dias_ate_vencimento,
    -- Total de serviços
    (SELECT COUNT(*) FROM contrato_servicos cs WHERE cs.codigo_contrato = ct.codigo_contrato) AS total_servicos
FROM contratos ct
         INNER JOIN clientes cl ON ct.codigo_cliente = cl.codigo_cliente
WHERE ct.status_contrato = 'ATIVO'
  AND cl.excluido_em IS NULL
ORDER BY ct.data_fim ASC;

COMMENT ON VIEW vw_contratos_ativos IS
'Visão de contratos ativos com informações do cliente e dias até vencimento';

-- =====================================================
-- FIM DA MIGRATION V004
-- =====================================================