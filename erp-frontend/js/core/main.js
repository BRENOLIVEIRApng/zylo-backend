/* ─── Zylo ERP · main.js ────────────────────────────────────────────────────
   Carregado em TODAS as páginas protegidas (após auth.js).
   Fornece: ZyloHttp, ZyloFormat e inicialização do layout.
────────────────────────────────────────────────────────────────────────────── */

document.addEventListener('DOMContentLoaded', () => {

  // ─── Sidebar toggle (mobile) ───────────────────────────────────────────────
  const sidebar       = document.getElementById('sidebar');
  const sidebarToggle = document.getElementById('sidebarToggle');

  sidebarToggle?.addEventListener('click', () => sidebar?.classList.toggle('open'));

  document.addEventListener('click', (e) => {
    if (sidebar && !sidebar.contains(e.target) && !sidebarToggle?.contains(e.target)) {
      sidebar.classList.remove('open');
    }
  });

  // ─── Preencher dados do usuário logado ────────────────────────────────────
  const usuario = ZyloAuth.getUsuario();

  if (usuario) {
    const iniciais = usuario.nomeCompleto
      ?.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase() || '?';

    document.querySelectorAll('[data-user-nome]').forEach(el => {
      el.textContent = usuario.nomeCompleto?.split(' ')[0] || usuario.nomeCompleto;
    });
    document.querySelectorAll('[data-user-perfil]').forEach(el => {
      el.textContent = usuario.perfil || '';
    });
    document.querySelectorAll('[data-user-iniciais]').forEach(el => {
      el.textContent = iniciais;
    });
    document.querySelectorAll('[data-user-email]').forEach(el => {
      el.textContent = usuario.email;
    });
  }

  // ─── Logout ───────────────────────────────────────────────────────────────
  document.querySelectorAll('[data-action="logout"]').forEach(el => {
    el.addEventListener('click', (e) => {
      e.preventDefault();
      if (confirm('Deseja realmente sair do sistema?')) ZyloAuth.logout();
    });
  });

  // ─── Nav link active state ─────────────────────────────────────────────────
  const currentPage = window.location.pathname.split('/').pop();
  document.querySelectorAll('.nav-link[data-page]').forEach(link => {
    if (link.dataset.page === currentPage) link.classList.add('active');
  });

  // ─── Bootstrap tooltips ───────────────────────────────────────────────────
  document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
    new bootstrap.Tooltip(el, { trigger: 'hover' });
  });

});

/* ─── HTTP Client ─────────────────────────────────────────────────────────────
   Wrapper do fetch com injeção automática de token JWT.
   Uso: ZyloHttp.get('/api/endpoint')
────────────────────────────────────────────────────────────────────────────── */
const ZyloHttp = (() => {

  const BASE = 'http://localhost:8080'; // Substituir pela URL de produção

  const request = async (method, endpoint, body) => {
    const res = await fetch(`${BASE}${endpoint}`, {
      method,
      headers: ZyloAuth.authHeaders(),
      ...(body !== undefined ? { body: JSON.stringify(body) } : {})
    });

    if (res.status === 401) {
      ZyloAuth.logout();
      return;
    }

    // Sem conteúdo (DELETE, PATCH sem body)
    if (res.status === 204) return null;

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      throw new Error(data?.mensagem || data?.message || `HTTP ${res.status}`);
    }

    return data;
  };

  return {
    get:    (ep)       => request('GET',    ep),
    post:   (ep, body) => request('POST',   ep, body),
    put:    (ep, body) => request('PUT',    ep, body),
    patch:  (ep, body) => request('PATCH',  ep, body ?? {}),
    delete: (ep)       => request('DELETE', ep)
  };

})();

/* ─── Formatadores ────────────────────────────────────────────────────────────
   Uso: ZyloFormat.moeda(1500) → "R$ 1.500,00"
────────────────────────────────────────────────────────────────────────────── */
const ZyloFormat = {
  moeda:    (v) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v || 0),
  data:     (v) => v ? new Date(v + 'T00:00:00').toLocaleDateString('pt-BR') : '—',
  dataHora: (v) => v ? new Date(v).toLocaleString('pt-BR') : '—',
  iniciais: (nome) => nome?.split(' ').slice(0, 2).map(n => n[0]).join('').toUpperCase() || '?',
  cnpj:     (v) => v?.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5') || v
};