-- =====================================================
-- V003__criar_tabelas_clientes.sql
-- Descrição: Estrutura completa de clientes (empresas B2B)
-- Autor: ERP Portal Team
-- Data: 2025-01-10
-- =====================================================

-- =====================================================
-- TABELA: Clientes (Empresas)
-- =====================================================
CREATE TABLE clientes (
                          codigo_cliente          BIGSERIAL PRIMARY KEY,

    -- Dados Cadastrais
                          razao_social            VARCHAR(200) NOT NULL,
                          nome_fantasia           VARCHAR(200),
                          cnpj                    VARCHAR(18) UNIQUE NOT NULL,  -- Formato: 00.000.000/0000-00
                          inscricao_estadual      VARCHAR(20),

    -- Endereço
                          cep                     VARCHAR(10),
                          logradouro              VARCHAR(200),
                          numero_endereco         VARCHAR(20),
                          complemento             VARCHAR(100),
                          bairro                  VARCHAR(100),
                          cidade                  VARCHAR(100),
                          estado                  CHAR(2),  -- UF: SP, RJ, MG, etc

    -- Status
                          status_cliente          VARCHAR(20) DEFAULT 'ATIVO' NOT NULL,

    -- Auditoria Completa
                          criado_em               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          criado_por              BIGINT NOT NULL,
                          atualizado_em           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          atualizado_por          BIGINT NOT NULL,
                          excluido_em             TIMESTAMP WITH TIME ZONE,
                          excluido_por            BIGINT,

    -- Foreign Keys
                          CONSTRAINT fk_clientes_criado_por
                              FOREIGN KEY (criado_por)
                                  REFERENCES usuarios(codigo_usuario)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_clientes_atualizado_por
                              FOREIGN KEY (atualizado_por)
                                  REFERENCES usuarios(codigo_usuario)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_clientes_excluido_por
                              FOREIGN KEY (excluido_por)
                                  REFERENCES usuarios(codigo_usuario)
                                  ON DELETE SET NULL,

    -- Constraints de Validação
                          CONSTRAINT chk_cliente_status CHECK (
                              status_cliente IN ('ATIVO', 'SUSPENSO', 'ENCERRADO')
                              ),

                          CONSTRAINT chk_cliente_estado CHECK (
                              estado IS NULL OR LENGTH(estado) = 2
                              ),

                          CONSTRAINT chk_cliente_cnpj_formato CHECK (
                              cnpj ~ '^\d{2}\.\d{3}\.\d{3}/\d{4}-\d{2}$'  -- Valida formato
),

    CONSTRAINT chk_cliente_razao_social_minimo CHECK (
        LENGTH(TRIM(razao_social)) >= 3
    ),

    CONSTRAINT chk_cliente_excluido_logica CHECK (
        (excluido_em IS NULL AND excluido_por IS NULL) OR
        (excluido_em IS NOT NULL AND excluido_por IS NOT NULL)
    )
);

-- =====================================================
-- ÍNDICES ESTRATÉGICOS - Clientes
-- =====================================================

-- Índice UNIQUE parcial: CNPJ único apenas entre clientes ativos
CREATE UNIQUE INDEX idx_clientes_cnpj_ativo
    ON clientes(cnpj)
    WHERE excluido_em IS NULL;

-- Índices para filtros comuns
CREATE INDEX idx_clientes_status ON clientes(status_cliente);
CREATE INDEX idx_clientes_cidade_estado ON clientes(cidade, estado);
CREATE INDEX idx_clientes_criado_em ON clientes(criado_em DESC);
CREATE INDEX idx_clientes_excluido ON clientes(excluido_em) WHERE excluido_em IS NOT NULL;

-- Índice para busca textual (nome/razão social)
CREATE INDEX idx_clientes_razao_social_lower ON clientes(LOWER(razao_social));
CREATE INDEX idx_clientes_nome_fantasia_lower ON clientes(LOWER(nome_fantasia)) WHERE nome_fantasia IS NOT NULL;

-- Índice composto para listagem padrão (status + nome)
CREATE INDEX idx_clientes_listagem ON clientes(status_cliente, razao_social);

-- Índice para auditoria (quem criou/atualizou)
CREATE INDEX idx_clientes_auditoria ON clientes(criado_por, atualizado_por);

-- =====================================================
-- COMENTÁRIOS - Clientes
-- =====================================================
COMMENT ON TABLE clientes IS
'Cadastro de empresas clientes (B2B) da software house';

COMMENT ON COLUMN clientes.status_cliente IS
'ATIVO: operando normalmente | SUSPENSO: temporariamente inativo (sem novos contratos) | ENCERRADO: relação comercial finalizada';

COMMENT ON COLUMN clientes.cnpj IS
'CNPJ formatado (00.000.000/0000-00). Único entre clientes ativos.';

COMMENT ON COLUMN clientes.excluido_em IS
'Soft delete: data/hora da exclusão. NULL = registro ativo';

-- =====================================================
-- TABELA: Contatos do Cliente
-- =====================================================
CREATE TABLE contatos_cliente (
                                  codigo_contato          BIGSERIAL PRIMARY KEY,
                                  codigo_cliente          BIGINT NOT NULL,

    -- Dados do Contato
                                  nome_contato            VARCHAR(100) NOT NULL,
                                  email_contato           VARCHAR(100),
                                  telefone                VARCHAR(20),
                                  celular                 VARCHAR(20),
                                  cargo                   VARCHAR(100),

    -- Flags
                                  principal               BOOLEAN DEFAULT FALSE NOT NULL,
                                  ativo                   BOOLEAN DEFAULT TRUE NOT NULL,

    -- Auditoria Básica
                                  criado_em               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  atualizado_em           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign Keys
                                  CONSTRAINT fk_contatos_cliente
                                      FOREIGN KEY (codigo_cliente)
                                          REFERENCES clientes(codigo_cliente)
                                          ON DELETE CASCADE,  -- Se cliente é excluído, contatos também são

    -- Constraints
                                  CONSTRAINT chk_contato_nome_minimo CHECK (
                                      LENGTH(TRIM(nome_contato)) >= 3
                                      ),

                                  CONSTRAINT chk_contato_email_formato CHECK (
                                      email_contato IS NULL OR
                                      email_contato ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
),

    CONSTRAINT chk_contato_pelo_menos_um_meio CHECK (
        email_contato IS NOT NULL OR telefone IS NOT NULL OR celular IS NOT NULL
    )
);

-- =====================================================
-- ÍNDICES - Contatos Cliente
-- =====================================================
CREATE INDEX idx_contatos_cliente ON contatos_cliente(codigo_cliente);
CREATE INDEX idx_contatos_principal ON contatos_cliente(codigo_cliente, principal) WHERE principal = TRUE;
CREATE INDEX idx_contatos_ativo ON contatos_cliente(ativo) WHERE ativo = TRUE;
CREATE INDEX idx_contatos_email ON contatos_cliente(LOWER(email_contato)) WHERE email_contato IS NOT NULL;

-- Índice parcial: apenas contatos ativos e principais
CREATE INDEX idx_contatos_ativos_principais
    ON contatos_cliente(codigo_cliente)
    WHERE ativo = TRUE AND principal = TRUE;

-- =====================================================
-- COMENTÁRIOS - Contatos
-- =====================================================
COMMENT ON TABLE contatos_cliente IS
'Pessoas de contato nas empresas clientes (pode haver múltiplos contatos por cliente)';

COMMENT ON COLUMN contatos_cliente.principal IS
'Indica o contato principal do cliente. Apenas UM contato por cliente deve ser principal.';

COMMENT ON COLUMN contatos_cliente.ativo IS
'Permite desativar contato sem excluir (pessoa saiu da empresa, etc)';

-- =====================================================
-- TABELA: Observações Internas sobre Clientes
-- =====================================================
CREATE TABLE observacoes_cliente (
                                     codigo_observacao           BIGSERIAL PRIMARY KEY,
                                     codigo_cliente              BIGINT NOT NULL,
                                     codigo_usuario              BIGINT NOT NULL,

    -- Conteúdo
                                     observacao                  TEXT NOT NULL,

    -- Metadata
                                     data_hora_observacao        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign Keys
                                     CONSTRAINT fk_observacoes_cliente
                                         FOREIGN KEY (codigo_cliente)
                                             REFERENCES clientes(codigo_cliente)
                                             ON DELETE CASCADE,

                                     CONSTRAINT fk_observacoes_usuario
                                         FOREIGN KEY (codigo_usuario)
                                             REFERENCES usuarios(codigo_usuario)
                                             ON DELETE RESTRICT,

    -- Constraints
                                     CONSTRAINT chk_observacao_minimo CHECK (
                                         LENGTH(TRIM(observacao)) >= 5
                                         )
);

-- =====================================================
-- ÍNDICES - Observações
-- =====================================================
CREATE INDEX idx_observacoes_cliente ON observacoes_cliente(codigo_cliente, data_hora_observacao DESC);
CREATE INDEX idx_observacoes_usuario ON observacoes_cliente(codigo_usuario);
CREATE INDEX idx_observacoes_data ON observacoes_cliente(data_hora_observacao DESC);

COMMENT ON TABLE observacoes_cliente IS
'Notas internas sobre clientes (não visíveis ao cliente). Útil para registrar histórico de interações.';

-- =====================================================
-- TRIGGERS: Atualização automática de timestamps
-- =====================================================
CREATE TRIGGER trg_clientes_atualizar_timestamp
    BEFORE UPDATE ON clientes
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_timestamp_atualizacao();

CREATE TRIGGER trg_contatos_atualizar_timestamp
    BEFORE UPDATE ON contatos_cliente
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_timestamp_atualizacao();

-- =====================================================
-- TRIGGER: Garantir apenas UM contato principal por cliente
-- =====================================================
CREATE OR REPLACE FUNCTION validar_contato_principal()
RETURNS TRIGGER AS $$
BEGIN
    -- Se está marcando como principal
    IF NEW.principal = TRUE THEN
        -- Desmarca outros contatos principais do mesmo cliente
UPDATE contatos_cliente
SET principal = FALSE
WHERE codigo_cliente = NEW.codigo_cliente
  AND codigo_contato != COALESCE(NEW.codigo_contato, 0)
          AND principal = TRUE;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_contatos_validar_principal
    BEFORE INSERT OR UPDATE ON contatos_cliente
                         FOR EACH ROW
                         EXECUTE FUNCTION validar_contato_principal();

COMMENT ON FUNCTION validar_contato_principal() IS
'Garante que apenas UM contato seja marcado como principal por cliente';

-- =====================================================
-- TRIGGER: Validar CNPJ antes de inserir/atualizar
-- =====================================================
CREATE OR REPLACE FUNCTION validar_cnpj_cliente()
RETURNS TRIGGER AS $$
BEGIN
    -- Remove formatação do CNPJ
    NEW.cnpj := REGEXP_REPLACE(NEW.cnpj, '[^0-9]', '', 'g');

    -- Valida formato básico
    IF NOT validar_formato_cnpj(NEW.cnpj) THEN
        RAISE EXCEPTION 'CNPJ inválido: %', NEW.cnpj;
END IF;

    -- Reformata com pontuação
    NEW.cnpj := formatar_cnpj(NEW.cnpj);

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_clientes_validar_cnpj
    BEFORE INSERT OR UPDATE ON clientes
                         FOR EACH ROW
                         EXECUTE FUNCTION validar_cnpj_cliente();

COMMENT ON FUNCTION validar_cnpj_cliente() IS
'Valida e formata CNPJ automaticamente antes de salvar';

-- =====================================================
-- TRIGGER: Impedir exclusão física de clientes com relacionamentos
-- =====================================================
CREATE OR REPLACE FUNCTION impedir_exclusao_cliente_com_relacionamentos()
RETURNS TRIGGER AS $$
DECLARE
tem_contratos INTEGER;
    tem_os INTEGER;
BEGIN
    -- Verifica se há contratos
SELECT COUNT(*) INTO tem_contratos
FROM contratos
WHERE codigo_cliente = OLD.codigo_cliente;

-- Verifica se há OS
SELECT COUNT(*) INTO tem_os
FROM ordens_servico
WHERE codigo_cliente = OLD.codigo_cliente;

-- Se tiver relacionamentos, impede exclusão física
IF tem_contratos > 0 OR tem_os > 0 THEN
        RAISE EXCEPTION 'Cliente possui % contratos e % ordens de serviço. Use exclusão lógica (soft delete).',
            tem_contratos, tem_os;
END IF;

RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Nota: Este trigger será ativado apenas na exclusão física (DELETE).
-- O soft delete (UPDATE com excluido_em) não dispara este trigger.
CREATE TRIGGER trg_clientes_impedir_exclusao
    BEFORE DELETE ON clientes
    FOR EACH ROW
    EXECUTE FUNCTION impedir_exclusao_cliente_com_relacionamentos();

COMMENT ON FUNCTION impedir_exclusao_cliente_com_relacionamentos() IS
'Impede exclusão física de clientes que possuem contratos ou OS. Força uso de soft delete.';

-- =====================================================
-- VIEWS ÚTEIS
-- =====================================================

-- View: Clientes Ativos com Contato Principal
CREATE OR REPLACE VIEW vw_clientes_ativos_com_contato AS
SELECT
    c.codigo_cliente,
    c.razao_social,
    c.nome_fantasia,
    c.cnpj,
    c.cidade,
    c.estado,
    c.status_cliente,
    cc.nome_contato AS contato_principal_nome,
    cc.email_contato AS contato_principal_email,
    cc.telefone AS contato_principal_telefone,
    c.criado_em
FROM clientes c
         LEFT JOIN contatos_cliente cc ON c.codigo_cliente = cc.codigo_cliente AND cc.principal = TRUE
WHERE c.excluido_em IS NULL
ORDER BY c.razao_social;

COMMENT ON VIEW vw_clientes_ativos_com_contato IS
'Visão simplificada de clientes ativos com seus contatos principais (para listagens)';

-- =====================================================
-- FIM DA MIGRATION V003
-- =====================================================