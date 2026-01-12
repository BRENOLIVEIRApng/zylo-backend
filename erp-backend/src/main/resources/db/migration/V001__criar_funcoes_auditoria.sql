-- =====================================================
-- V001__criar_funcoes_auditoria.sql
-- Descrição: Funções e triggers para auditoria automática
-- Autor: Breno Olivera Alves
-- Data: 2025-01-10
-- =====================================================

-- =====================================================
-- EXTENSION PARA UUID (caso precise no futuro)
-- =====================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- FUNÇÃO: Atualizar timestamp de atualização automaticamente
-- =====================================================
CREATE OR REPLACE FUNCTION atualizar_timestamp_atualizacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION atualizar_timestamp_atualizacao() IS
'Trigger function que atualiza automaticamente o campo atualizado_em';

-- =====================================================
-- FUNÇÃO: Validar CNPJ (formato básico)
-- =====================================================
CREATE OR REPLACE FUNCTION validar_formato_cnpj(cnpj_input VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    -- Remove caracteres não numéricos
    cnpj_input := REGEXP_REPLACE(cnpj_input, '[^0-9]', '', 'g');

    -- Verifica se tem 14 dígitos
    IF LENGTH(cnpj_input) != 14 THEN
        RETURN FALSE;
END IF;

    -- Verifica se não são todos dígitos iguais
    IF cnpj_input ~ '^(\d)\1{13}$' THEN
        RETURN FALSE;
END IF;

RETURN TRUE;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION validar_formato_cnpj(VARCHAR) IS
'Valida o formato básico de um CNPJ (14 dígitos, não todos iguais)';

-- =====================================================
-- FUNÇÃO: Formatar CNPJ (adicionar pontuação)
-- =====================================================
CREATE OR REPLACE FUNCTION formatar_cnpj(cnpj_input VARCHAR)
RETURNS VARCHAR AS $$
DECLARE
cnpj_limpo VARCHAR;
BEGIN
    -- Remove caracteres não numéricos
    cnpj_limpo := REGEXP_REPLACE(cnpj_input, '[^0-9]', '', 'g');

    -- Se não tiver 14 dígitos, retorna como está
    IF LENGTH(cnpj_limpo) != 14 THEN
        RETURN cnpj_input;
END IF;

    -- Formata: 00.000.000/0000-00
RETURN SUBSTRING(cnpj_limpo, 1, 2) || '.' ||
       SUBSTRING(cnpj_limpo, 3, 3) || '.' ||
       SUBSTRING(cnpj_limpo, 6, 3) || '/' ||
       SUBSTRING(cnpj_limpo, 9, 4) || '-' ||
       SUBSTRING(cnpj_limpo, 13, 2);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION formatar_cnpj(VARCHAR) IS
'Formata CNPJ no padrão 00.000.000/0000-00';

-- =====================================================
-- FUNÇÃO: Gerar número sequencial (formato customizado)
-- =====================================================
CREATE OR REPLACE FUNCTION gerar_numero_sequencial(
    prefixo VARCHAR,
    sequence_name VARCHAR,
    tamanho_numero INTEGER DEFAULT 6
)
RETURNS VARCHAR AS $$
DECLARE
ano_atual VARCHAR(4);
    proximo_numero BIGINT;
    numero_formatado VARCHAR;
BEGIN
    -- Pega o ano atual
    ano_atual := EXTRACT(YEAR FROM CURRENT_DATE)::VARCHAR;

    -- Pega o próximo valor da sequence
EXECUTE format('SELECT nextval(%L)', sequence_name) INTO proximo_numero;

-- Formata o número com zeros à esquerda
numero_formatado := LPAD(proximo_numero::VARCHAR, tamanho_numero, '0');

    -- Retorna no formato: PREFIXO-ANO-NUMERO
    -- Exemplo: CT-2025-000001
RETURN prefixo || '-' || ano_atual || '-' || numero_formatado;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION gerar_numero_sequencial(VARCHAR, VARCHAR, INTEGER) IS
'Gera números sequenciais no formato PREFIXO-ANO-NUMERO (ex: CT-2025-000001)';

-- =====================================================
-- FUNÇÃO: Calcular data limite com SLA (apenas horas úteis)
-- Considera: Seg-Sex, 8h-18h, sem feriados
-- =====================================================
CREATE OR REPLACE FUNCTION calcular_data_limite_sla(
    data_inicio TIMESTAMP,
    sla_horas INTEGER
)
RETURNS TIMESTAMP AS $$
DECLARE
data_atual TIMESTAMP;
    horas_restantes INTEGER;
    dia_semana INTEGER;
BEGIN
    data_atual := data_inicio;
    horas_restantes := sla_horas;

    -- Se SLA é 0 ou negativo, retorna data_inicio
    IF sla_horas <= 0 THEN
        RETURN data_inicio;
END IF;

    WHILE horas_restantes > 0 LOOP
        -- Avança 1 hora
        data_atual := data_atual + INTERVAL '1 hour';

        -- Pega o dia da semana (0=Domingo, 6=Sábado)
        dia_semana := EXTRACT(DOW FROM data_atual);

        -- Se for dia útil (Seg-Sex) e horário comercial (8h-18h)
        IF dia_semana BETWEEN 1 AND 5 AND
           EXTRACT(HOUR FROM data_atual) BETWEEN 8 AND 17 THEN
            horas_restantes := horas_restantes - 1;
END IF;
END LOOP;

RETURN data_atual;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION calcular_data_limite_sla(TIMESTAMP, INTEGER) IS
'Calcula data limite considerando apenas horas úteis (Seg-Sex, 8h-18h)';

-- =====================================================
-- FUNÇÃO: Log de auditoria completo (armazena JSON)
-- =====================================================
CREATE OR REPLACE FUNCTION registrar_auditoria()
RETURNS TRIGGER AS $$
DECLARE
codigo_usuario_atual BIGINT;
BEGIN
    -- Tenta pegar o código do usuário da sessão
    -- (será setado pelo backend via SET LOCAL)
BEGIN
        codigo_usuario_atual := CURRENT_SETTING('app.codigo_usuario', TRUE)::BIGINT;
EXCEPTION WHEN OTHERS THEN
        codigo_usuario_atual := NULL;
END;

    -- INSERT
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO auditoria (
            tabela_auditada,
            codigo_registro,
            operacao,
            codigo_usuario,
            data_hora_operacao,
            dados_anteriores,
            dados_novos
        ) VALUES (
            TG_TABLE_NAME,
            (NEW.codigo_cliente)::BIGINT,  -- Ajustar conforme a PK da tabela
            'INSERT',
            codigo_usuario_atual,
            CURRENT_TIMESTAMP,
            NULL,
            row_to_json(NEW)
        );
RETURN NEW;

-- UPDATE
ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO auditoria (
            tabela_auditada,
            codigo_registro,
            operacao,
            codigo_usuario,
            data_hora_operacao,
            dados_anteriores,
            dados_novos
        ) VALUES (
            TG_TABLE_NAME,
            (NEW.codigo_cliente)::BIGINT,
            'UPDATE',
            codigo_usuario_atual,
            CURRENT_TIMESTAMP,
            row_to_json(OLD),
            row_to_json(NEW)
        );
RETURN NEW;

-- DELETE
ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO auditoria (
            tabela_auditada,
            codigo_registro,
            operacao,
            codigo_usuario,
            data_hora_operacao,
            dados_anteriores,
            dados_novos
        ) VALUES (
            TG_TABLE_NAME,
            (OLD.codigo_cliente)::BIGINT,
            'DELETE',
            codigo_usuario_atual,
            CURRENT_TIMESTAMP,
            row_to_json(OLD),
            NULL
        );
RETURN OLD;
END IF;

RETURN NULL;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION registrar_auditoria() IS
'Registra todas as operações (INSERT, UPDATE, DELETE) na tabela de auditoria com dados em JSON';

-- =====================================================
-- TABELA: Auditoria Global (criada aqui para uso nas triggers)
-- =====================================================
CREATE TABLE IF NOT EXISTS auditoria (
                                         codigo_auditoria    BIGSERIAL PRIMARY KEY,
                                         tabela_auditada     VARCHAR(100) NOT NULL,
    codigo_registro     BIGINT NOT NULL,
    operacao            VARCHAR(20) NOT NULL CHECK (operacao IN ('INSERT', 'UPDATE', 'DELETE')),
    codigo_usuario      BIGINT,  -- Pode ser NULL se ação do sistema
    data_hora_operacao  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                      dados_anteriores    JSONB,  -- NULL para INSERT
                                      dados_novos         JSONB   -- NULL para DELETE
                                      );

-- Índices para consultas rápidas na auditoria
CREATE INDEX idx_auditoria_tabela_registro ON auditoria(tabela_auditada, codigo_registro);
CREATE INDEX idx_auditoria_usuario ON auditoria(codigo_usuario);
CREATE INDEX idx_auditoria_data ON auditoria(data_hora_operacao DESC);
CREATE INDEX idx_auditoria_operacao ON auditoria(operacao);

COMMENT ON TABLE auditoria IS
'Tabela central de auditoria - registra todas as operações críticas do sistema';

COMMENT ON COLUMN auditoria.dados_anteriores IS
'Snapshot do registro ANTES da alteração (JSON completo)';

COMMENT ON COLUMN auditoria.dados_novos IS
'Snapshot do registro DEPOIS da alteração (JSON completo)';

-- =====================================================
-- SEQUENCES para numeração sequencial
-- Serão usadas pelas funções de geração de números
-- =====================================================

-- Sequence para Contratos
CREATE SEQUENCE IF NOT EXISTS seq_numero_contrato START 1 INCREMENT 1;
COMMENT ON SEQUENCE seq_numero_contrato IS 'Sequence para numeração de contratos (CT-2025-000001)';

-- Sequence para Ordens de Serviço
CREATE SEQUENCE IF NOT EXISTS seq_numero_os START 1 INCREMENT 1;
COMMENT ON SEQUENCE seq_numero_os IS 'Sequence para numeração de OS (OS-2025-000001)';

-- Sequence para Faturas
CREATE SEQUENCE IF NOT EXISTS seq_numero_fatura START 1 INCREMENT 1;
COMMENT ON SEQUENCE seq_numero_fatura IS 'Sequence para numeração de faturas (FAT-2025-000001)';

-- =====================================================
-- FIM DA MIGRATION V001
-- =====================================================