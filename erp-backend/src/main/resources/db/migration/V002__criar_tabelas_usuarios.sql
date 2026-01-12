-- =====================================================
-- V002__criar_tabelas_usuarios.sql
-- Descrição: Estrutura completa de usuários, perfis e permissões (RBAC)
-- Autor: Breno Olirveira Alves
-- Data: 2025-01-10
-- =====================================================

-- =====================================================
-- TABELA: Perfis (Roles)
-- =====================================================
CREATE TABLE perfis (
                        codigo_perfil       BIGSERIAL PRIMARY KEY,
                        nome_perfil         VARCHAR(50) UNIQUE NOT NULL,
                        descricao_perfil    TEXT,
                        sistema             BOOLEAN DEFAULT FALSE NOT NULL,  -- Se TRUE, não pode ser excluído
                        criado_em           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

                        CONSTRAINT chk_perfil_nome_minimo CHECK (LENGTH(TRIM(nome_perfil)) >= 3)
);

-- Índices
CREATE INDEX idx_perfis_nome ON perfis(nome_perfil);
CREATE INDEX idx_perfis_sistema ON perfis(sistema) WHERE sistema = TRUE;

-- Comentários
COMMENT ON TABLE perfis IS
'Perfis de acesso (Roles) do sistema - Admin, Gestor, Financeiro, Operacional';

COMMENT ON COLUMN perfis.sistema IS
'Indica se é perfil do sistema (não pode ser excluído). Admin, Gestor, etc são perfis do sistema.';

-- =====================================================
-- TABELA: Permissões
-- =====================================================
CREATE TABLE permissoes (
                            codigo_permissao    BIGSERIAL PRIMARY KEY,
                            modulo              VARCHAR(50) NOT NULL,  -- CLIENTES, CONTRATOS, OS, etc
                            acao                VARCHAR(20) NOT NULL,  -- VER, CRIAR, EDITAR, EXCLUIR
                            descricao_permissao TEXT,

                            CONSTRAINT uk_permissao_modulo_acao UNIQUE (modulo, acao),
                            CONSTRAINT chk_permissao_acao CHECK (acao IN ('VER', 'CRIAR', 'EDITAR', 'EXCLUIR'))
);

-- Índices
CREATE INDEX idx_permissoes_modulo ON permissoes(modulo);
CREATE INDEX idx_permissoes_acao ON permissoes(acao);

-- Comentários
COMMENT ON TABLE permissoes IS
'Permissões granulares do sistema - define ações possíveis por módulo';

COMMENT ON COLUMN permissoes.modulo IS
'Módulo do sistema: CLIENTES, CONTRATOS, SERVICOS, OS, FATURAMENTO, USUARIOS';

COMMENT ON COLUMN permissoes.acao IS
'Ação permitida: VER (read), CRIAR (create), EDITAR (update), EXCLUIR (delete)';

-- =====================================================
-- TABELA: Perfil_Permissões (N:N)
-- =====================================================
CREATE TABLE perfil_permissoes (
                                   codigo_perfil       BIGINT NOT NULL,
                                   codigo_permissao    BIGINT NOT NULL,

                                   PRIMARY KEY (codigo_perfil, codigo_permissao),

                                   CONSTRAINT fk_perfil_permissoes_perfil
                                       FOREIGN KEY (codigo_perfil)
                                           REFERENCES perfis(codigo_perfil)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_perfil_permissoes_permissao
                                       FOREIGN KEY (codigo_permissao)
                                           REFERENCES permissoes(codigo_permissao)
                                           ON DELETE CASCADE
);

-- Índices para otimizar joins
CREATE INDEX idx_perfil_permissoes_perfil ON perfil_permissoes(codigo_perfil);
CREATE INDEX idx_perfil_permissoes_permissao ON perfil_permissoes(codigo_permissao);

COMMENT ON TABLE perfil_permissoes IS
'Tabela associativa entre Perfis e Permissões - define quais permissões cada perfil possui';

-- =====================================================
-- TABELA: Usuários
-- =====================================================
CREATE TABLE usuarios (
                          codigo_usuario      BIGSERIAL PRIMARY KEY,
                          nome_completo       VARCHAR(100) NOT NULL,
                          email               VARCHAR(100) UNIQUE NOT NULL,
                          senha_hash          VARCHAR(255) NOT NULL,  -- BCrypt hash
                          codigo_perfil       BIGINT NOT NULL,
                          ativo               BOOLEAN DEFAULT TRUE NOT NULL,

    -- Auditoria
                          criado_em           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          atualizado_em       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          excluido_em         TIMESTAMP WITH TIME ZONE,
                          excluido_por        BIGINT,

    -- Constraints
                          CONSTRAINT fk_usuarios_perfil
                              FOREIGN KEY (codigo_perfil)
                                  REFERENCES perfis(codigo_perfil)
                                  ON DELETE RESTRICT,  -- Não permite excluir perfil se houver usuários

                          CONSTRAINT fk_usuarios_excluido_por
                              FOREIGN KEY (excluido_por)
                                  REFERENCES usuarios(codigo_usuario)
                                  ON DELETE SET NULL,

                          CONSTRAINT chk_usuario_nome_minimo CHECK (LENGTH(TRIM(nome_completo)) >= 3),
                          CONSTRAINT chk_usuario_email_formato CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_usuario_excluido_logica CHECK (
        (excluido_em IS NULL AND excluido_por IS NULL) OR
        (excluido_em IS NOT NULL AND excluido_por IS NOT NULL)
    )
);

-- Índices estratégicos
CREATE UNIQUE INDEX idx_usuarios_email_ativo
    ON usuarios(LOWER(email))
    WHERE excluido_em IS NULL;  -- Email único apenas entre usuários ativos

CREATE INDEX idx_usuarios_perfil ON usuarios(codigo_perfil);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo) WHERE ativo = TRUE;
CREATE INDEX idx_usuarios_nome ON usuarios(nome_completo);
CREATE INDEX idx_usuarios_excluido ON usuarios(excluido_em) WHERE excluido_em IS NOT NULL;

-- Comentários
COMMENT ON TABLE usuarios IS
'Usuários do sistema - pessoas com acesso ao ERP';

COMMENT ON COLUMN usuarios.senha_hash IS
'Hash BCrypt da senha (NUNCA armazenar senha em texto plano)';

COMMENT ON COLUMN usuarios.ativo IS
'Indica se usuário pode fazer login (diferente de excluído)';

COMMENT ON COLUMN usuarios.excluido_em IS
'Data/hora de exclusão lógica (soft delete). NULL = não excluído';

-- =====================================================
-- TABELA: Histórico de Acessos (Login Logs)
-- =====================================================
CREATE TABLE historico_acessos (
                                   codigo_acesso       BIGSERIAL PRIMARY KEY,
                                   codigo_usuario      BIGINT NOT NULL,
                                   ip_acesso           VARCHAR(45),  -- Suporta IPv6
                                   user_agent          TEXT,  -- Informações do navegador
                                   data_hora_acesso    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   sucesso             BOOLEAN DEFAULT TRUE NOT NULL,
                                   motivo_falha        VARCHAR(255),  -- Senha incorreta, usuário inativo, etc

                                   CONSTRAINT fk_historico_acessos_usuario
                                       FOREIGN KEY (codigo_usuario)
                                           REFERENCES usuarios(codigo_usuario)
                                           ON DELETE CASCADE
);

-- Índices para consultas comuns
CREATE INDEX idx_historico_usuario ON historico_acessos(codigo_usuario);
CREATE INDEX idx_historico_data DESC ON historico_acessos(data_hora_acesso DESC);
CREATE INDEX idx_historico_sucesso ON historico_acessos(sucesso) WHERE sucesso = FALSE;
CREATE INDEX idx_historico_ip ON historico_acessos(ip_acesso);

-- Índice composto para "últimos acessos do usuário"
CREATE INDEX idx_historico_usuario_data ON historico_acessos(codigo_usuario, data_hora_acesso DESC);

COMMENT ON TABLE historico_acessos IS
'Registro de todas as tentativas de login (bem-sucedidas ou não)';

COMMENT ON COLUMN historico_acessos.sucesso IS
'TRUE = login bem-sucedido, FALSE = falha (senha incorreta, usuário bloqueado, etc)';

-- =====================================================
-- TRIGGERS: Atualização automática de timestamps
-- =====================================================
CREATE TRIGGER trg_usuarios_atualizar_timestamp
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_timestamp_atualizacao();

-- =====================================================
-- TRIGGERS: Auditoria (registra mudanças críticas)
-- =====================================================
-- Nota: A função registrar_auditoria() precisa ser adaptada para cada tabela
-- por causa do nome da PK. Vou criar triggers específicos depois.

-- =====================================================
-- ÍNDICES ADICIONAIS PARA PERFORMANCE
-- =====================================================

-- Índice parcial: apenas usuários ativos e não excluídos
CREATE INDEX idx_usuarios_ativos_completo
    ON usuarios(codigo_usuario, nome_completo, email)
    WHERE ativo = TRUE AND excluido_em IS NULL;

-- Índice para busca case-insensitive por nome
CREATE INDEX idx_usuarios_nome_lower ON usuarios(LOWER(nome_completo));

-- =====================================================
-- PARTICIONAMENTO (Opcional - para grande volume)
-- =====================================================
-- Se historico_acessos crescer muito, considere particionar por data:
-- CREATE TABLE historico_acessos_2025_01 PARTITION OF historico_acessos
--     FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

-- =====================================================
-- CONSTRAINTS ADICIONAIS DE SEGURANÇA
-- =====================================================

-- Impede que o último admin seja desativado
-- (será implementado via trigger ou lógica de aplicação)

-- =====================================================
-- FIM DA MIGRATION V002
-- =====================================================