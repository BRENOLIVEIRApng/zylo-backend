-- DADOS INICIAIS (SEED DATA)

-- PERFIS PADRÃO DO SISTEMA
INSERT INTO perfis (codigo_perfil, nome_perfil, descricao_perfil, sistema) VALUES
                                                                               (1, 'Admin', 'Administrador com acesso total ao sistema', TRUE),
                                                                               (2, 'Gestor', 'Gestor com acesso gerencial (sem usuários)', TRUE),
                                                                               (3, 'Financeiro', 'Acesso apenas ao módulo financeiro', TRUE),
                                                                               (4, 'Operacional', 'Acesso a OS e clientes', TRUE);

-- PERMISSÕES DO SISTEMA
INSERT INTO permissoes (codigo_permissao, modulo, acao, descricao_permissao) VALUES
-- CLIENTES
(1, 'CLIENTES', 'VER', 'Visualizar clientes'),
(2, 'CLIENTES', 'CRIAR', 'Criar novos clientes'),
(3, 'CLIENTES', 'EDITAR', 'Editar clientes'),
(4, 'CLIENTES', 'EXCLUIR', 'Excluir clientes'),

-- CONTRATOS
(5, 'CONTRATOS', 'VER', 'Visualizar contratos'),
(6, 'CONTRATOS', 'CRIAR', 'Criar novos contratos'),
(7, 'CONTRATOS', 'EDITAR', 'Editar contratos'),
(8, 'CONTRATOS', 'EXCLUIR', 'Excluir contratos'),

-- SERVICOS
(9, 'SERVICOS', 'VER', 'Visualizar serviços'),
(10, 'SERVICOS', 'CRIAR', 'Criar novos serviços'),
(11, 'SERVICOS', 'EDITAR', 'Editar serviços'),
(12, 'SERVICOS', 'EXCLUIR', 'Excluir serviços'),

-- OS
(13, 'OS', 'VER', 'Visualizar ordens de serviço'),
(14, 'OS', 'CRIAR', 'Criar novas OS'),
(15, 'OS', 'EDITAR', 'Editar OS'),
(16, 'OS', 'EXCLUIR', 'Excluir OS'),

-- FATURAMENTO
(17, 'FATURAMENTO', 'VER', 'Visualizar faturas'),
(18, 'FATURAMENTO', 'CRIAR', 'Criar faturas'),
(19, 'FATURAMENTO', 'EDITAR', 'Editar faturas'),
(20, 'FATURAMENTO', 'EXCLUIR', 'Excluir faturas'),

-- USUARIOS
(21, 'USUARIOS', 'VER', 'Visualizar usuários'),
(22, 'USUARIOS', 'CRIAR', 'Criar novos usuários'),
(23, 'USUARIOS', 'EDITAR', 'Editar usuários'),
(24, 'USUARIOS', 'EXCLUIR', 'Excluir usuários');

-- PERMISSÕES DO PERFIL ADMIN (todas)
INSERT INTO perfil_permissoes (codigo_perfil, codigo_permissao)
SELECT 1, codigo_permissao FROM permissoes;

-- PERMISSÕES DO PERFIL GESTOR (tudo exceto usuários)
INSERT INTO perfil_permissoes (codigo_perfil, codigo_permissao)
SELECT 2, codigo_permissao FROM permissoes WHERE modulo != 'USUARIOS';

-- PERMISSÕES DO PERFIL FINANCEIRO (apenas faturamento e visualização)
INSERT INTO perfil_permissoes (codigo_perfil, codigo_permissao) VALUES
                                                                    (3, 1), (3, 5), (3, 9), (3, 13), (3, 17), (3, 18), (3, 19), (3, 20);

-- PERMISSÕES DO PERFIL OPERACIONAL (OS completo + clientes leitura)
INSERT INTO perfil_permissoes (codigo_perfil, codigo_permissao) VALUES
                                                                    (4, 1), (4, 3), (4, 5), (4, 13), (4, 14), (4, 15);

-- USUÁRIO ADMIN PADRÃO
-- Senha: admin123 (hash BCrypt)
INSERT INTO usuarios (codigo_usuario, nome_completo, email, senha_hash, codigo_perfil, ativo)
VALUES (1, 'Administrador do Sistema', 'admin@erpportal.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIr7eZPvNu',
        1, TRUE);

-- RESETAR SEQUENCES
SELECT setval('seq_numero_contrato', 1, false);
SELECT setval('seq_numero_os', 1, false);
SELECT setval('seq_numero_fatura', 1, false);