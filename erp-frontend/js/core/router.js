/* ─── Zylo ERP · router.js ──────────────────────────────────────────────────
   Guardas de rota e helpers de navegação.
   Depende de: auth.js (ZyloAuth)
────────────────────────────────────────────────────────────────────────────── */

const ZyloRouter = (() => {

  // --Mapa: arquivo HTML → permissão mínima necessária
  const _rotasProtegidas = {
    'usuarios-lista.html':   'USUARIOS:VER',
    'usuarios-detalhe.html': 'USUARIOS:VER',
    'perfis-lista.html':     'USUARIOS:VER',
    'clientes-lista.html':   'CLIENTES:VER',
    'contratos-lista.html':  'CONTRATOS:VER',
    'servicos-lista.html':   'SERVICOS:VER',
    'os-kanban.html':        'OS:VER',
    'faturas-lista.html':    'FATURAMENTO:VER',
  };

  // --Exigir autenticação — usa ZyloAuth._navTo internamente
  const requireAuth = () => {
    if (!ZyloAuth.isLogado()) {
      ZyloAuth.logout(); // logout já redireciona para index.html
      return false;
    }
    return true;
  };

  // --Verificar permissão mínima
  const checkPermissao = () => {
    const pagina = window.location.pathname.split('/').pop();
    const permissaoNecessaria = _rotasProtegidas[pagina];
    if (!permissaoNecessaria) return true;

    const usuario = ZyloAuth.getUsuario();
    // Fallback permissivo — enquanto JWT não traz mapa de permissões completo
    if (!usuario?.permissoes) return true;

    const temAcesso = usuario.permissoes.some(p => p === permissaoNecessaria);
    if (!temAcesso) {
      window.location.href = ZyloAuth.baseUrl + '/pages/dashboard/home.html';
      return false;
    }
    return true;
  };

  // --Inicializar proteção de rota (chamar no topo de cada página interna)
  const init = () => {
    requireAuth();
    checkPermissao();
  };

  // --Navegar para uma URL relativa ao projeto
  const goTo = (relativePath) => {
    window.location.href = ZyloAuth.baseUrl + relativePath;
  };

  // --Ler parâmetro da query string
  const param = (key) => new URLSearchParams(window.location.search).get(key);

  return { init, requireAuth, checkPermissao, goTo, param };

})();