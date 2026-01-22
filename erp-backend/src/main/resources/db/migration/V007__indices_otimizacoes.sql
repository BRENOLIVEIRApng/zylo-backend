-- ÍNDICES ADICIONAIS PARA PERFORMANCE

-- Índices compostos para queries comuns
CREATE INDEX idx_clientes_status_cidade ON clientes(status_cliente, cidade) WHERE excluido_em IS NULL;
CREATE INDEX idx_contratos_cliente_status_tipo ON contratos(codigo_cliente, status_contrato, tipo_contrato);
CREATE INDEX idx_os_status_responsavel_prioridade ON ordens_servico(status_os, codigo_responsavel, prioridade);
CREATE INDEX idx_faturas_cliente_status_vencimento ON faturas(codigo_cliente, status_fatura, data_vencimento);

-- Índices para relatórios
CREATE INDEX idx_os_data_criacao_mes ON ordens_servico(criado_em);
CREATE INDEX idx_faturas_emissao_mes ON faturas(data_emissao);

-- Índices de texto para busca
CREATE INDEX idx_clientes_busca ON clientes USING gin(to_tsvector('portuguese', razao_social || ' ' || COALESCE(nome_fantasia, '')));
CREATE INDEX idx_os_busca ON ordens_servico USING gin(to_tsvector('portuguese', titulo_os || ' ' || COALESCE(descricao_os, '')));

-- Estatísticas
ANALYZE usuarios;
ANALYZE perfis;
ANALYZE clientes;
ANALYZE contratos;
ANALYZE servicos;
ANALYZE ordens_servico;
ANALYZE faturas;