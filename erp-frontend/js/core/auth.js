/* ─── Zylo ERP · auth.js ────────────────────────────────────────────────────
   Gerencia autenticação e sessão.
   Carregado em TODAS as páginas (public e protegidas).

   ⚠️  IMPORTANTE — PATHS DE REDIRECIONAMENTO:
   Nunca use paths absolutos com barra inicial ("/index.html") pois o servidor
   IntelliJ serve em /zylo-prod/erp-frontend/, não na raiz.
   Use window.location.origin + pathname calculado, ou paths relativos.
────────────────────────────────────────────────────────────────────────────── */

const API_BASE = 'http://localhost:8080'; // URL do backend Spring Boot

/* ── Detectar o base path do projeto automaticamente ───────────────────────
   Funciona tanto em localhost:63342/zylo-prod/erp-frontend/ quanto em
   localhost:8080/ ou qualquer deploy.
   Ex: se pathname é /zylo-prod/erp-frontend/index.html
       _BASE_URL  → /zylo-prod/erp-frontend
────────────────────────────────────────────────────────────────────────────── */
const _BASE_URL = (() => {
  const path = window.location.pathname;
  // Procura o segmento "erp-frontend" como âncora do projeto
  const marker = 'erp-frontend';
  const idx = path.indexOf(marker);
  if (idx !== -1) return path.slice(0, idx + marker.length);
  // Fallback: usa a raiz
  return '';
})();

const ZyloAuth = (() => {

  const TOKEN_KEY = 'zylo_token';
  const USER_KEY  = 'zylo_user';

  // --Helpers de sessão
  const saveSession  = (data) => {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify(data.usuario));
  };

  const clearSession = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  };

  const getToken   = () => localStorage.getItem(TOKEN_KEY);
  const getUsuario = () => JSON.parse(localStorage.getItem(USER_KEY) || 'null');
  const isLogado   = () => !!getToken();

  const authHeaders = () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${getToken()}`
  });

  // --Navegação segura — usa BASE_URL calculado, nunca path absoluto fixo
  const _navTo = (relativePath) => {
    window.location.href = _BASE_URL + relativePath;
  };

  // ─── Login ───────────────────────────────────────────────────────────────
  const login = async (email, senha) => {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, senha })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.mensagem || data.message || 'Email ou senha incorretos');

    saveSession(data);
    return data;
  };

  // ─── Logout ───────────────────────────────────────────────────────────────
  const logout = async () => {
    try {
      await fetch(`${API_BASE}/api/auth/logout`, {
        method: 'POST',
        headers: authHeaders()
      });
    } catch (_) { /* JWT é stateless */ }

    clearSession();
    _navTo('/index.html');
  };

  // ─── Proteção de rota ─────────────────────────────────────────────────────
  const requireAuth = () => {
    if (!isLogado()) {
      _navTo('/index.html');
      return false;
    }
    return true;
  };

  // ─── Redirecionar se já logado (páginas públicas) ─────────────────────────
  const redirectIfLogado = () => {
    if (isLogado()) {
      _navTo('/pages/dashboard/home.html');
    }
  };

  // ─── Reset de senha ───────────────────────────────────────────────────────
  const solicitarResetSenha = async (email) => {
    try {
      const res = await fetch(`${API_BASE}/api/auth/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });
      if (!res.ok && res.status !== 404) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.mensagem || data.message || 'Erro ao solicitar redefinição');
      }
    } catch (err) {
      if (!err.message.includes('fetch') && !err.message.includes('404')) throw err;
    }
    return true;
  };

  return {
    login, logout, requireAuth, redirectIfLogado,
    getUsuario, getToken, authHeaders, isLogado,
    solicitarResetSenha,
    baseUrl: _BASE_URL   // expõe para uso em outros módulos
  };

})();