/* ─── Zylo ERP · auth.js ────────────────────────────────────────────────────── */

const API_BASE = 'http://localhost:8080'; // --Substituir pela URL de produção

const ZyloAuth = (() => {

  const TOKEN_KEY = 'zylo_token';
  const USER_KEY  = 'zylo_user';

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

  // --Autenticar usuário
  const login = async (email, senha) => {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, senha })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Email ou senha incorretos');

    saveSession(data);
    return data;
  };

  // --Encerrar sessão
  const logout = async () => {
    try {
      await fetch(`${API_BASE}/api/auth/logout`, {
        method: 'POST',
        headers: authHeaders()
      });
    } catch (_) { /* stateless — ignora erros de rede */ }

    clearSession();
    window.location.href = '/index.html';
  };

  // --Redirecionar para login se não autenticado
  const requireAuth = () => {
    if (!isLogado()) window.location.href = '/index.html';
  };

  // --Redirecionar para home se já autenticado
  const redirectIfLogado = () => {
    if (isLogado()) window.location.href = '/pages/dashboard/home.html';
  };

  // --Solicitar redefinição de senha
  const solicitarResetSenha = async (email) => {
    const res = await fetch(`${API_BASE}/api/auth/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      throw new Error(data.message || 'Erro ao solicitar redefinição');
    }
    return true;
  };

  return { login, logout, requireAuth, redirectIfLogado, getUsuario, getToken, authHeaders, isLogado };
})();

/* ─── UI helpers ─────────────────────────────────────────────────────────────── */
const ZyloUI = (() => {

  // Bootstrap Icons por tipo de alerta
  const alertIcons = {
    error:   'bi-exclamation-triangle-fill',
    success: 'bi-check-circle-fill',
    info:    'bi-info-circle-fill'
  };

  // --Exibir alerta inline
  const showAlert = (containerId, mensagem, tipo = 'error') => {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = `
      <div class="zylo-alert ${tipo}" role="alert">
        <i class="bi ${alertIcons[tipo] || 'bi-info-circle-fill'}"></i>
        <span>${mensagem}</span>
      </div>`;
  };

  const clearAlert = (containerId) => {
    const el = document.getElementById(containerId);
    if (el) el.innerHTML = '';
  };

  // --Estado de loading no botão
  const btnLoading = (btn, loading = true, texto = 'Aguarde...') => {
    if (loading) {
      btn.dataset.originalHtml = btn.innerHTML;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>${texto}`;
      btn.disabled = true;
    } else {
      btn.innerHTML = btn.dataset.originalHtml || btn.innerHTML;
      btn.disabled = false;
    }
  };

  // --Toggle visibilidade da senha
  const toggleSenha = (inputId, btnId) => {
    const input = document.getElementById(inputId);
    const btn   = document.getElementById(btnId);
    if (!input || !btn) return;
    const visible = input.type === 'text';
    input.type = visible ? 'password' : 'text';
    const icon = btn.querySelector('i');
    if (icon) icon.className = visible ? 'bi bi-eye' : 'bi bi-eye-slash';
  };

  return { showAlert, clearAlert, btnLoading, toggleSenha };
})();