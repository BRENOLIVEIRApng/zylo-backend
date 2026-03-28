/* ─── Zylo ERP · topbar.js ──────────────────────────────────────────────────
   Topbar com breadcrumb dinâmico e dropdown do usuário.
   Depende de: auth.js, utils.js (ZyloAuth, ZyloFormat)
────────────────────────────────────────────────────────────────────────────── */

const ZyloTopbar = (() => {

  const render = ({ title = '', subtitle = '' } = {}) => {
    const mountPoint = document.getElementById('topbar');
    if (!mountPoint) return;

    const usuario      = ZyloAuth.getUsuario();
    const iniciais     = ZyloFormat.iniciais(usuario?.nomeCompleto || '');
    const primeiroNome = usuario?.nomeCompleto?.split(' ')[0] || '—';
    const base         = ZyloAuth.baseUrl;

    mountPoint.innerHTML = `
      <!-- --Toggle sidebar mobile -->
      <button class="topbar-toggle" id="sidebarToggle" aria-label="Abrir menu">
        <i class="bi bi-list"></i>
      </button>

      <!-- --Breadcrumb -->
      <div class="topbar-breadcrumb">
        <h3 id="topbarTitle">${title}</h3>
        <span id="topbarSubtitle">${subtitle}</span>
      </div>

      <!-- --Ações -->
      <div class="topbar-actions">

        <button class="topbar-icon-btn" title="Notificações">
          <i class="bi bi-bell"></i>
        </button>

        <!-- --Dropdown do usuário -->
        <div class="topbar-user-wrap" id="topbarUserWrap">

          <button class="topbar-user" id="topbarUserBtn"
            aria-haspopup="true" aria-expanded="false">
            <div class="topbar-user-avatar">${iniciais}</div>
            <span class="topbar-user-name">${primeiroNome}</span>
            <i class="bi bi-chevron-down topbar-chevron"></i>
          </button>

          <div class="topbar-dropdown" id="topbarDropdown" role="menu">

            <div class="dropdown-header">
              <div class="dropdown-avatar">${iniciais}</div>
              <div>
                <div class="dropdown-nome">${usuario?.nomeCompleto || '—'}</div>
                <div class="dropdown-email">${usuario?.email || ''}</div>
                <div class="dropdown-perfil">${usuario?.perfil || ''}</div>
              </div>
            </div>

            <div class="dropdown-divider"></div>

            <a href="${base}/pages/admin/usuarios-detalhe.html?id=${usuario?.codigoUsuario || ''}"
               class="dropdown-item" role="menuitem">
              <i class="bi bi-person-circle"></i> Minha Conta
            </a>
            <a href="${base}/pages/admin/usuarios-detalhe.html?id=${usuario?.codigoUsuario || ''}#senha"
               class="dropdown-item" role="menuitem">
              <i class="bi bi-key"></i> Alterar Senha
            </a>

            <div class="dropdown-divider"></div>

            <a href="#" class="dropdown-item dropdown-item-danger"
               data-action="logout" role="menuitem">
              <i class="bi bi-box-arrow-right"></i> Sair do Sistema
            </a>

          </div>
        </div>

      </div>`;

    _bindEvents(mountPoint);
  };

  const setTitle = (title, subtitle = '') => {
    const t = document.getElementById('topbarTitle');
    const s = document.getElementById('topbarSubtitle');
    if (t) t.textContent = title;
    if (s) s.textContent = subtitle;
  };

  const _bindEvents = (topbar) => {
    const btn  = topbar.querySelector('#topbarUserBtn');
    const wrap = topbar.querySelector('#topbarUserWrap');
    if (!btn || !wrap) return;

    // --Toggle dropdown
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const open = wrap.classList.toggle('open');
      btn.setAttribute('aria-expanded', String(open));
    });

    // --Fechar ao clicar fora
    document.addEventListener('click', (e) => {
      if (!wrap.contains(e.target)) {
        wrap.classList.remove('open');
        btn.setAttribute('aria-expanded', 'false');
      }
    });

    // --Fechar com Escape
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        wrap.classList.remove('open');
        btn.setAttribute('aria-expanded', 'false');
      }
    });

    // --Logout
    topbar.querySelectorAll('[data-action="logout"]').forEach(el => {
      el.addEventListener('click', (e) => {
        e.preventDefault();
        if (confirm('Deseja realmente sair do sistema?')) ZyloAuth.logout();
      });
    });

    // --Toggle sidebar mobile
    topbar.querySelector('#sidebarToggle')?.addEventListener('click', () => {
      document.getElementById('sidebar')?.classList.toggle('open');
    });
  };

  return { render, setTitle };

})();