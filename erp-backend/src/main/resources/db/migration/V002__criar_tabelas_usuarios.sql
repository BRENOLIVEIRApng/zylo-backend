-- =====================================================
-- V002__CRIAR_TABELAS_USUARIOS.SQL (CORRIGIDO)
-- =====================================================

-- =====================================================
-- TABELA: PERFIS
-- =====================================================
CREATE TABLE PERFIS (
                        CODIGO_PERFIL       BIGSERIAL PRIMARY KEY,
                        NOME_PERFIL         VARCHAR(50) UNIQUE NOT NULL,
                        DESCRICAO_PERFIL    TEXT,
                        SISTEMA             BOOLEAN DEFAULT FALSE NOT NULL,
                        CRIADO_EM           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

                        CONSTRAINT CHK_PERFIL_NOME_MINIMO CHECK (LENGTH(TRIM(NOME_PERFIL)) >= 3)
);

CREATE INDEX IDX_PERFIS_NOME ON PERFIS(NOME_PERFIL);
CREATE INDEX IDX_PERFIS_SISTEMA ON PERFIS(SISTEMA) WHERE SISTEMA = TRUE;

COMMENT ON TABLE PERFIS IS 'PERFIS DE ACESSO (ROLES) DO SISTEMA';

-- =====================================================
-- TABELA: PERMISSÕES
-- =====================================================
CREATE TABLE PERMISSOES (
                            CODIGO_PERMISSAO    BIGSERIAL PRIMARY KEY,
                            MODULO              VARCHAR(50) NOT NULL,
                            ACAO                VARCHAR(20) NOT NULL,
                            DESCRICAO_PERMISSAO TEXT,

                            CONSTRAINT UK_PERMISSAO_MODULO_ACAO UNIQUE (MODULO, ACAO),
                            CONSTRAINT CHK_PERMISSAO_ACAO CHECK (ACAO IN ('VER', 'CRIAR', 'EDITAR', 'EXCLUIR'))
);

CREATE INDEX IDX_PERMISSOES_MODULO ON PERMISSOES(MODULO);
CREATE INDEX IDX_PERMISSOES_ACAO ON PERMISSOES(ACAO);

COMMENT ON TABLE PERMISSOES IS 'PERMISSÕES GRANULARES DO SISTEMA';

-- =====================================================
-- TABELA: PERFIL_PERMISSÕES (N:N)
-- =====================================================
CREATE TABLE PERFIL_PERMISSOES (
                                   CODIGO_PERFIL       BIGINT NOT NULL,
                                   CODIGO_PERMISSAO    BIGINT NOT NULL,

                                   PRIMARY KEY (CODIGO_PERFIL, CODIGO_PERMISSAO),

                                   CONSTRAINT FK_PERFIL_PERMISSOES_PERFIL
                                       FOREIGN KEY (CODIGO_PERFIL)
                                           REFERENCES PERFIS(CODIGO_PERFIL)
                                           ON DELETE CASCADE,

                                   CONSTRAINT FK_PERFIL_PERMISSOES_PERMISSAO
                                       FOREIGN KEY (CODIGO_PERMISSAO)
                                           REFERENCES PERMISSOES(CODIGO_PERMISSAO)
                                           ON DELETE CASCADE
);

CREATE INDEX IDX_PERFIL_PERMISSOES_PERFIL ON PERFIL_PERMISSOES(CODIGO_PERFIL);
CREATE INDEX IDX_PERFIL_PERMISSOES_PERMISSAO ON PERFIL_PERMISSOES(CODIGO_PERMISSAO);

-- =====================================================
-- TABELA: USUÁRIOS (CORRIGIDA)
-- =====================================================
CREATE TABLE USUARIOS (
                          CODIGO_USUARIO      BIGSERIAL PRIMARY KEY,
                          NOME_COMPLETO       VARCHAR(100) NOT NULL,
                          EMAIL               VARCHAR(100) UNIQUE NOT NULL,
                          SENHA_HASH          VARCHAR(255) NOT NULL,
                          CODIGO_PERFIL       BIGINT NOT NULL,
                          ATIVO               BOOLEAN DEFAULT TRUE NOT NULL,
                          ULTIMO_ACESSO       TIMESTAMP WITH TIME ZONE,

    -- AUDITORIA (NULLABLE PARA PERMITIR PRIMEIRO USUÁRIO)
                          CRIADO_EM           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          CRIADO_POR          BIGINT,
                          ATUALIZADO_EM       TIMESTAMP WITH TIME ZONE,
                          ATUALIZADO_POR      BIGINT,
                          EXCLUIDO_EM         TIMESTAMP WITH TIME ZONE,
                          EXCLUIDO_POR        BIGINT,

    -- CONSTRAINTS
                          CONSTRAINT FK_USUARIOS_PERFIL
                              FOREIGN KEY (CODIGO_PERFIL)
                                  REFERENCES PERFIS(CODIGO_PERFIL)
                                  ON DELETE RESTRICT,

                          CONSTRAINT CHK_USUARIO_NOME_MINIMO
                              CHECK (LENGTH(TRIM(NOME_COMPLETO)) >= 3),

                          CONSTRAINT CHK_USUARIO_EMAIL_FORMATO
                              CHECK (EMAIL ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),

                          CONSTRAINT CHK_USUARIO_EXCLUIDO_LOGICA
                              CHECK (
                                  (EXCLUIDO_EM IS NULL AND EXCLUIDO_POR IS NULL) OR
                                  (EXCLUIDO_EM IS NOT NULL AND EXCLUIDO_POR IS NOT NULL)
                                  )
);

-- ÍNDICES
CREATE UNIQUE INDEX IDX_USUARIOS_EMAIL_ATIVO
    ON USUARIOS(LOWER(EMAIL))
    WHERE EXCLUIDO_EM IS NULL;

CREATE INDEX IDX_USUARIOS_PERFIL ON USUARIOS(CODIGO_PERFIL);
CREATE INDEX IDX_USUARIOS_ATIVO ON USUARIOS(ATIVO) WHERE ATIVO = TRUE;
CREATE INDEX IDX_USUARIOS_NOME ON USUARIOS(NOME_COMPLETO);
CREATE INDEX IDX_USUARIOS_EXCLUIDO ON USUARIOS(EXCLUIDO_EM) WHERE EXCLUIDO_EM IS NOT NULL;
CREATE INDEX IDX_USUARIOS_NOME_LOWER ON USUARIOS(LOWER(NOME_COMPLETO));

CREATE INDEX IDX_USUARIOS_ATIVOS_COMPLETO
    ON USUARIOS(CODIGO_USUARIO, NOME_COMPLETO, EMAIL)
    WHERE ATIVO = TRUE AND EXCLUIDO_EM IS NULL;

COMMENT ON TABLE USUARIOS IS 'USUÁRIOS DO SISTEMA';
COMMENT ON COLUMN USUARIOS.ULTIMO_ACESSO IS 'DATA/HORA DO ÚLTIMO LOGIN BEM-SUCEDIDO';

-- =====================================================
-- TABELA: HISTÓRICO DE ACESSOS
-- =====================================================
CREATE TABLE HISTORICO_ACESSOS (
                                   CODIGO_ACESSO       BIGSERIAL PRIMARY KEY,
                                   CODIGO_USUARIO      BIGINT NOT NULL,
                                   IP_ACESSO           VARCHAR(45),
                                   USER_AGENT          TEXT,
                                   DATA_HORA_ACESSO    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   SUCESSO             BOOLEAN DEFAULT TRUE NOT NULL,
                                   MOTIVO_FALHA        VARCHAR(255),

                                   CONSTRAINT FK_HISTORICO_ACESSOS_USUARIO
                                       FOREIGN KEY (CODIGO_USUARIO)
                                           REFERENCES USUARIOS(CODIGO_USUARIO)
                                           ON DELETE CASCADE
);

CREATE INDEX IDX_HISTORICO_USUARIO ON HISTORICO_ACESSOS(CODIGO_USUARIO);
CREATE INDEX IDX_HISTORICO_DATA ON HISTORICO_ACESSOS(DATA_HORA_ACESSO DESC);
CREATE INDEX IDX_HISTORICO_SUCESSO ON HISTORICO_ACESSOS(SUCESSO) WHERE SUCESSO = FALSE;
CREATE INDEX IDX_HISTORICO_IP ON HISTORICO_ACESSOS(IP_ACESSO);
CREATE INDEX IDX_HISTORICO_USUARIO_DATA ON HISTORICO_ACESSOS(CODIGO_USUARIO, DATA_HORA_ACESSO DESC);

COMMENT ON TABLE HISTORICO_ACESSOS IS 'REGISTRO DE TENTATIVAS DE LOGIN';