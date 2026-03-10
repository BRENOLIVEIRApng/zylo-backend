/* ─── Zylo ERP · api.js ─────────────────────────────────────────────────────
   Registro de endpoints por módulo.
   Depende de: auth.js + main.js (ZyloAuth + ZyloHttp)
   Carregar APÓS auth.js e main.js nas páginas protegidas.
────────────────────────────────────────────────────────────────────────────── */

const ZyloAPI = (() => {

  // ─── Usuários ─────────────────────────────────────────────────────────────
  const Usuarios = {
    listar:       ()         => ZyloHttp.get('/api/usuarios'),
    buscarPorId:  (id)       => ZyloHttp.get(`/api/usuarios/${id}`),
    me:           ()         => ZyloHttp.get('/api/usuarios/me'),
    criar:        (dto)      => ZyloHttp.post('/api/usuarios', dto),
    editar:       (id, dto)  => ZyloHttp.put(`/api/usuarios/${id}`, dto),
    desativar:    (id)       => ZyloHttp.patch(`/api/usuarios/${id}/desativar`),
    ativar:       (id)       => ZyloHttp.patch(`/api/usuarios/${id}/ativar`),
    resetarSenha: (id, novaSenha)            => ZyloHttp.patch(`/api/usuarios/${id}/resetar-senha`, { novaSenha }),
    alterarSenha: (senhaAtual, novaSenha)    => ZyloHttp.patch('/api/usuarios/alterar-senha', { senhaAtual, novaSenha }),
    listarPorPerfil: (codigoPerfil)          => ZyloHttp.get(`/api/usuarios/perfil/${codigoPerfil}`),
    listarPorStatus: (ativo)                 => ZyloHttp.get(`/api/usuarios/status/${ativo}`)
  };

  // ─── Perfis ───────────────────────────────────────────────────────────────
  const Perfis = {
    listar:              ()          => ZyloHttp.get('/api/perfis'),
    listarSistema:       ()          => ZyloHttp.get('/api/perfis/sistema'),
    listarPersonalizados:()          => ZyloHttp.get('/api/perfis/personalizados'),
    buscarPorId:         (id)        => ZyloHttp.get(`/api/perfis/${id}`),
    listarPermissoes:    (id)        => ZyloHttp.get(`/api/perfis/${id}/permissoes`),
    criar:               (dto)       => ZyloHttp.post('/api/perfis', dto),
    editar:              (id, dto)   => ZyloHttp.put(`/api/perfis/${id}`, dto),
    excluir:             (id)        => ZyloHttp.delete(`/api/perfis/${id}`),
    sincronizarPermissoes:(id, ids)  => ZyloHttp.put(`/api/perfis/${id}/permissoes`, { permissoesIds: ids }),
    adicionarPermissao:  (id, codPerm) => ZyloHttp.post(`/api/perfis/${id}/permissoes/${codPerm}`),
    removerPermissao:    (id, codPerm) => ZyloHttp.delete(`/api/perfis/${id}/permissoes/${codPerm}`)
  };

  // ─── Módulos futuros (esqueleto) ──────────────────────────────────────────
  // Os endpoints abaixo serão implementados conforme o backend avançar.

  const Clientes = {
    listar:      ()        => ZyloHttp.get('/api/clientes'),
    buscarPorId: (id)      => ZyloHttp.get(`/api/clientes/${id}`),
    criar:       (dto)     => ZyloHttp.post('/api/clientes', dto),
    editar:      (id, dto) => ZyloHttp.put(`/api/clientes/${id}`, dto),
    desativar:   (id)      => ZyloHttp.patch(`/api/clientes/${id}/desativar`)
  };

  const Contratos = {
    listar:      ()        => ZyloHttp.get('/api/contratos'),
    buscarPorId: (id)      => ZyloHttp.get(`/api/contratos/${id}`),
    criar:       (dto)     => ZyloHttp.post('/api/contratos', dto),
    editar:      (id, dto) => ZyloHttp.put(`/api/contratos/${id}`, dto),
    suspender:   (id, dto) => ZyloHttp.patch(`/api/contratos/${id}/suspender`, dto),
    cancelar:    (id, dto) => ZyloHttp.patch(`/api/contratos/${id}/cancelar`, dto)
  };

  const Servicos = {
    listar:      ()        => ZyloHttp.get('/api/servicos'),
    buscarPorId: (id)      => ZyloHttp.get(`/api/servicos/${id}`),
    criar:       (dto)     => ZyloHttp.post('/api/servicos', dto),
    editar:      (id, dto) => ZyloHttp.put(`/api/servicos/${id}`, dto)
  };

  const OrdensServico = {
    listar:       ()            => ZyloHttp.get('/api/ordens-servico'),
    buscarPorId:  (id)          => ZyloHttp.get(`/api/ordens-servico/${id}`),
    criar:        (dto)         => ZyloHttp.post('/api/ordens-servico', dto),
    editar:       (id, dto)     => ZyloHttp.put(`/api/ordens-servico/${id}`, dto),
    mudarStatus:  (id, dto)     => ZyloHttp.patch(`/api/ordens-servico/${id}/status`, dto),
    atribuir:     (id, usuId)   => ZyloHttp.patch(`/api/ordens-servico/${id}/responsavel`, { codigoUsuario: usuId }),
    comentar:     (id, texto)   => ZyloHttp.post(`/api/ordens-servico/${id}/comentarios`, { texto }),
    finalizar:    (id, dto)     => ZyloHttp.patch(`/api/ordens-servico/${id}/finalizar`, dto)
  };

  const Faturamento = {
    listar:           ()        => ZyloHttp.get('/api/faturas'),
    buscarPorId:      (id)      => ZyloHttp.get(`/api/faturas/${id}`),
    criar:            (dto)     => ZyloHttp.post('/api/faturas', dto),
    marcarPago:       (id, dto) => ZyloHttp.patch(`/api/faturas/${id}/pagar`, dto),
    cancelar:         (id, dto) => ZyloHttp.patch(`/api/faturas/${id}/cancelar`, dto)
  };

  const Dashboard = {
    metricas: () => ZyloHttp.get('/api/dashboard/metricas')
  };

  return { Usuarios, Perfis, Clientes, Contratos, Servicos, OrdensServico, Faturamento, Dashboard };

})();