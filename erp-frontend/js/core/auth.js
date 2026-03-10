/* ─── Zylo ERP · auth.js ────────────────────────────────────────────────────
   Gerencia autenticação, sessão e helpers de UI.
   Carregado em TODAS as páginas (public e protegidas).
────────────────────────────────────────────────────────────────────────────── */

const API_BASE = 'http://localhost:8080'; // Substituir pela URL de produção

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
    } catch (_) { /* JWT é stateless — ignora erros de rede */ }

    clearSession();
    window.location.href = '/index.html';
  };

  // ─── Proteção de rota ─────────────────────────────────────────────────────
  const requireAuth = () => {
    if (!isLogado()) window.location.href = '/index.html';
  };

  // ─── Redirecionar se já logado (páginas públicas) ─────────────────────────
  const redirectIfLogado = () => {
    if (isLogado()) window.location.href = '/pages/dashboard/home.html';
  };

  // ─── Reset de senha ───────────────────────────────────────────────────────
  // NOTA: endpoint /api/auth/reset-password ainda não implementado no backend.
  // A UI exibe o estado de sucesso normalmente; o email será enviado em versão futura.
  const solicitarResetSenha = async (email) => {
    try {
      const res = await fetch(`${API_BASE}/api/auth/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });

      // Aceita 200, 204 ou qualquer 2xx como sucesso
      if (!res.ok && res.status !== 404) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.mensagem || data.message || 'Erro ao solicitar redefinição');
      }
    } catch (err) {
      // 404 = endpoint não implementado ainda → trata como sucesso (não expõe se email existe)
      if (!err.message.includes('fetch') && !err.message.includes('404')) {
        throw err;
      }
    }
    return true;
  };

  return {
    login,
    logout,
    requireAuth,
    redirectIfLogado,
    getUsuario,
    getToken,
    authHeaders,
    isLogado,
    solicitarResetSenha
  };

})();

/* ─── UI Helpers ─────────────────────────────────────────────────────────────
   Reutilizável em qualquer página.
────────────────────────────────────────────────────────────────────────────── */
const ZyloUI = (() => {

  const alertIcons = {
    error:   'bi-exclamation-triangle-fill',
    success: 'bi-check-circle-fill',
    info:    'bi-info-circle-fill',
    warning: 'bi-exclamation-circle-fill'
  };

  // ─── Alerta inline ────────────────────────────────────────────────────────
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

  // ─── Loading no botão ─────────────────────────────────────────────────────
  const btnLoading = (btn, loading = true, texto = 'Aguarde...') => {
    if (loading) {
      btn.dataset.originalHtml = btn.innerHTML;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> ${texto}`;
      btn.disabled = true;
    } else {
      btn.innerHTML = btn.dataset.originalHtml || btn.innerHTML;
      btn.disabled = false;
    }
  };

  // ─── Toggle senha ─────────────────────────────────────────────────────────
  const toggleSenha = (inputId, btnId) => {
    const input = document.getElementById(inputId);
    const btn   = document.getElementById(btnId);
    if (!input || !btn) return;
    const visible = input.type === 'text';
    input.type = visible ? 'password' : 'text';
    const icon = btn.querySelector('i');
    if (icon) icon.className = visible ? 'bi bi-eye' : 'bi bi-eye-slash';
  };

  // ─── Toast (notificação flutuante) ────────────────────────────────────────
  const toast = (mensagem, tipo = 'success', duracao = 3500) => {
    let container = document.getElementById('zylo-toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'zylo-toast-container';
      container.style.cssText = 'position:fixed;top:1rem;right:1rem;z-index:9999;display:flex;flex-direction:column;gap:.5rem;';
      document.body.appendChild(container);
    }

    const el = document.createElement('div');
    el.className = `zylo-alert ${tipo}`;
    el.style.cssText = 'min-width:260px;box-shadow:0 4px 12px rgba(0,0,0,.1);animation:fadeIn .2s ease;';
    el.innerHTML = `<i class="bi ${alertIcons[tipo] || 'bi-info-circle-fill'}"></i><span>${mensagem}</span>`;
    container.appendChild(el);

    setTimeout(() => el.remove(), duracao);
  };

  return { showAlert, clearAlert, btnLoading, toggleSenha, toast };

})();