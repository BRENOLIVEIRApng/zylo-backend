-- FATURAS
CREATE TABLE faturas (
                         codigo_fatura BIGSERIAL PRIMARY KEY,
                         numero_fatura VARCHAR(20) UNIQUE NOT NULL,  -- FAT-2025-000001
                         codigo_cliente BIGINT NOT NULL,
                         codigo_contrato BIGINT,
                         data_emissao DATE NOT NULL,
                         data_vencimento DATE NOT NULL,
                         periodo_inicio DATE,
                         periodo_fim DATE,
                         valor_total NUMERIC(10,2) NOT NULL,
                         status_fatura VARCHAR(20) DEFAULT 'PENDENTE' NOT NULL,
                         data_pagamento DATE,
                         forma_pagamento VARCHAR(50),
                         observacoes_fatura TEXT,
                         gerada_automaticamente BOOLEAN DEFAULT FALSE NOT NULL,
                         criado_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                         criado_por BIGINT NOT NULL,

                         CONSTRAINT fk_faturas_cliente FOREIGN KEY (codigo_cliente) REFERENCES clientes(codigo_cliente) ON DELETE RESTRICT,
                         CONSTRAINT fk_faturas_contrato FOREIGN KEY (codigo_contrato) REFERENCES contratos(codigo_contrato) ON DELETE RESTRICT,
                         CONSTRAINT fk_faturas_criado_por FOREIGN KEY (criado_por) REFERENCES usuarios(codigo_usuario) ON DELETE RESTRICT,

                         CONSTRAINT chk_fatura_status CHECK (status_fatura IN ('PENDENTE', 'PAGO', 'ATRASADO', 'CANCELADO')),
                         CONSTRAINT chk_fatura_valor_positivo CHECK (valor_total >= 0),
                         CONSTRAINT chk_fatura_datas CHECK (data_vencimento >= data_emissao),
                         CONSTRAINT chk_fatura_periodo CHECK (periodo_fim IS NULL OR periodo_fim >= periodo_inicio),
                         CONSTRAINT chk_fatura_pagamento CHECK (
                             (status_fatura = 'PAGO' AND data_pagamento IS NOT NULL AND forma_pagamento IS NOT NULL) OR
                             (status_fatura != 'PAGO' AND data_pagamento IS NULL)
                             )
);

-- ÍNDICES
CREATE UNIQUE INDEX idx_faturas_numero ON faturas(numero_fatura);
CREATE INDEX idx_faturas_cliente ON faturas(codigo_cliente);
CREATE INDEX idx_faturas_contrato ON faturas(codigo_contrato);
CREATE INDEX idx_faturas_status ON faturas(status_fatura);
CREATE INDEX idx_faturas_vencimento ON faturas(data_vencimento);
CREATE INDEX idx_faturas_emissao ON faturas(data_emissao DESC);

-- Índice para faturas atrasadas
CREATE INDEX idx_faturas_atrasadas
    ON faturas(data_vencimento)
    WHERE status_fatura IN ('PENDENTE', 'ATRASADO') AND data_vencimento IS NOT NULL;

-- Índice para faturamento mensal
CREATE INDEX idx_faturas_periodo ON faturas(data_emissao, data_vencimento);

-- ITENS DA FATURA
CREATE TABLE fatura_itens (
                              codigo_item BIGSERIAL PRIMARY KEY,
                              codigo_fatura BIGINT NOT NULL,
                              codigo_servico BIGINT NOT NULL,
                              descricao_item VARCHAR(200),
                              quantidade INTEGER DEFAULT 1 NOT NULL,
                              valor_unitario NUMERIC(10,2) NOT NULL,
                              valor_total_item NUMERIC(10,2) NOT NULL,

                              CONSTRAINT fk_fatura_itens_fatura FOREIGN KEY (codigo_fatura) REFERENCES faturas(codigo_fatura) ON DELETE CASCADE,
                              CONSTRAINT fk_fatura_itens_servico FOREIGN KEY (codigo_servico) REFERENCES servicos(codigo_servico) ON DELETE RESTRICT,

                              CONSTRAINT chk_item_quantidade_positiva CHECK (quantidade > 0),
                              CONSTRAINT chk_item_valor_unitario CHECK (valor_unitario >= 0),
                              CONSTRAINT chk_item_valor_total CHECK (valor_total_item = valor_unitario * quantidade)
);

CREATE INDEX idx_fatura_itens_fatura ON fatura_itens(codigo_fatura);

-- VINCULAÇÃO FATURA <-> OS
CREATE TABLE fatura_os (
                           codigo_fatura BIGINT NOT NULL,
                           codigo_os BIGINT NOT NULL,

                           PRIMARY KEY (codigo_fatura, codigo_os),

                           CONSTRAINT fk_fatura_os_fatura FOREIGN KEY (codigo_fatura) REFERENCES faturas(codigo_fatura) ON DELETE CASCADE,
                           CONSTRAINT fk_fatura_os_os FOREIGN KEY (codigo_os) REFERENCES ordens_servico(codigo_os) ON DELETE CASCADE
);

CREATE INDEX idx_fatura_os_fatura ON fatura_os(codigo_fatura);
CREATE INDEX idx_fatura_os_os ON fatura_os(codigo_os);

-- TRIGGER: Gerar número da fatura
CREATE OR REPLACE FUNCTION gerar_numero_fatura()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.numero_fatura IS NULL OR NEW.numero_fatura = '' THEN
        NEW.numero_fatura := gerar_numero_sequencial('FAT', 'seq_numero_fatura', 6);
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_faturas_gerar_numero
    BEFORE INSERT ON faturas
    FOR EACH ROW
    EXECUTE FUNCTION gerar_numero_fatura();

-- TRIGGER: Recalcular valor total da fatura
CREATE OR REPLACE FUNCTION recalcular_valor_total_fatura()
RETURNS TRIGGER AS $$
DECLARE
novo_valor_total NUMERIC(10,2);
BEGIN
SELECT COALESCE(SUM(valor_total_item), 0)
INTO novo_valor_total
FROM fatura_itens
WHERE codigo_fatura = COALESCE(NEW.codigo_fatura, OLD.codigo_fatura);

UPDATE faturas
SET valor_total = novo_valor_total
WHERE codigo_fatura = COALESCE(NEW.codigo_fatura, OLD.codigo_fatura);

RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_fatura_itens_recalcular_valor_insert
    AFTER INSERT ON fatura_itens FOR EACH ROW EXECUTE FUNCTION recalcular_valor_total_fatura();

CREATE TRIGGER trg_fatura_itens_recalcular_valor_update
    AFTER UPDATE ON fatura_itens FOR EACH ROW EXECUTE FUNCTION recalcular_valor_total_fatura();

CREATE TRIGGER trg_fatura_itens_recalcular_valor_delete
    AFTER DELETE ON fatura_itens FOR EACH ROW EXECUTE FUNCTION recalcular_valor_total_fatura();

-- TRIGGER: Atualizar status para ATRASADO automaticamente
-- (Será executado por JOB diário no backend, não trigger por performance)