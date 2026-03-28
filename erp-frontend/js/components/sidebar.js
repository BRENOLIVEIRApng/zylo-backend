/* ─── Zylo ERP · sidebar.js ─────────────────────────────────────────────────
   Sidebar icon-rail com flyout controlado por JS.
   Hover → mouseenter/mouseleave (desktop)
   Clique → toggle (mobile/touch)
   Depende de: auth.js, utils.js (ZyloAuth, ZyloFormat)
────────────────────────────────────────────────────────────────────────────── */

const ZyloSidebar = (() => {

  // --Definição centralizada da navegação
  // ⚠️  Links usam BASE relativo ao projeto, não absoluto.
  //     ZyloAuth.baseUrl é calculado automaticamente em auth.js.
  const _getNav = () => {
    const base = ZyloAuth.baseUrl;
    return [
      {
        icon: 'bi-house',
        label: 'Dashboard',
        links: [
          { href: `${base}/pages/dashboard/home.html`, label: 'Início', page: 'home.html' }
        ]
      },
      {
        icon: 'bi-buildings',
        label: 'Clientes',
        links: [
          { href: `${base}/pages/clientes/clientes-lista.html`,   label: 'Lista de Clientes', page: 'clientes-lista.html' },
          { href: `${base}/pages/clientes/clientes-form.html`,    label: 'Novo Cliente',      page: 'clientes-form.html'  }
        ]
      },
      {
        icon: 'bi-file-earmark-text',
        label: 'Contratos',
        links: [
          { href: `${base}/pages/contratos/contratos-lista.html`,  label: 'Lista de Contratos', page: 'contratos-lista.html'  },
          { href: `${base}/pages/contratos/contratos-wizard.html`, label: 'Novo Contrato',       page: 'contratos-wizard.html' }
        ]
      },
      {
        icon: 'bi-gear',
        label: 'Serviços',
        links: [
          { href: `${base}/pages/servicos/servicos-lista.html`,   label: 'Catálogo',     page: 'servicos-lista.html' },
          { href: `${base}/pages/servicos/servicos-form.html`,    label: 'Novo Serviço', page: 'servicos-form.html'  }
        ]
      },
      {
        icon: 'bi-kanban',
        label: 'Ordens de Serviço',
        badge: 'badgeOS',
        links: [
          { href: `${base}/pages/ordens-servico/os-kanban.html`,  label: 'Kanban',      page: 'os-kanban.html' },
          { href: `${base}/pages/ordens-servico/os-lista.html`,   label: 'Lista de OS', page: 'os-lista.html'  },
          { href: `${base}/pages/ordens-servico/os-form.html`,    label: 'Nova OS',     page: 'os-form.html'   }
        ]
      },
      {
        icon: 'bi-receipt',
        label: 'Faturamento',
        links: [
          { href: `${base}/pages/faturamento/faturas-lista.html`, label: 'Faturas',     page: 'faturas-lista.html' },
          { href: `${base}/pages/faturamento/faturas-form.html`,  label: 'Nova Fatura', page: 'faturas-form.html'  }
        ]
      },
      { divider: true },
      {
        icon: 'bi-people',
        label: 'Usuários',
        links: [
          { href: `${base}/pages/admin/usuarios-lista.html`,      label: 'Lista de Usuários', page: 'usuarios-lista.html'   },
          { href: `${base}/pages/admin/usuarios-detalhe.html`,    label: 'Meu Perfil',        page: 'usuarios-detalhe.html' }
        ]
      },
      {
        icon: 'bi-shield-lock',
        label: 'Perfis e Permissões',
        links: [
          { href: `${base}/pages/admin/perfis-lista.html`,        label: 'Perfis', page: 'perfis-lista.html' }
        ]
      }
    ];
  };

  // --Verifica se algum link do grupo é a página atual
  const _isAtivo = (grupo, currentPage) =>
    grupo.links?.some(l => l.page === currentPage);

  // --HTML de um item
  const _renderItem = (grupo, currentPage) => {
    if (grupo.divider) return `<div class="sidebar-divider"></div>`;

    const ativo     = _isAtivo(grupo, currentPage);
    const linksHtml = grupo.links.map(l => {
      const linkAtivo = l.page === currentPage;
      return `<a href="${l.href}" class="flyout-link${linkAtivo ? ' active' : ''}">${l.label}</a>`;
    }).join('');

    return `
      <div class="nav-rail-item${ativo ? ' active' : ''}">
        <div class="nav-rail-btn" title="${grupo.label}">
          <i class="bi ${grupo.icon}"></i>
          ${grupo.badge ? `<span class="nav-rail-badge" id="${grupo.badge}"></span>` : ''}
        </div>
        <div class="nav-flyout">
          <span class="flyout-title">${grupo.label}</span>
          ${linksHtml}
        </div>
      </div>`;
  };

  // --Fechar todos os flyouts
  const _fecharTodos = (mountPoint) => {
    mountPoint.querySelectorAll('.nav-rail-item.flyout-open')
      .forEach(el => el.classList.remove('flyout-open'));
  };

  // --Bind hover + clique em cada item
  const _bindItem = (item, mountPoint) => {
    let closeTimer = null;

    item.addEventListener('mouseenter', () => {
      clearTimeout(closeTimer);
      _fecharTodos(mountPoint);
      item.classList.add('flyout-open');
    });

    item.addEventListener('mouseleave', () => {
      closeTimer = setTimeout(() => item.classList.remove('flyout-open'), 150);
    });

    const flyout = item.querySelector('.nav-flyout');
    flyout?.addEventListener('mouseenter', () => clearTimeout(closeTimer));
    flyout?.addEventListener('mouseleave', () => {
      closeTimer = setTimeout(() => item.classList.remove('flyout-open'), 150);
    });

    // Clique (toggle — mobile/teclado)
    item.querySelector('.nav-rail-btn')?.addEventListener('click', (e) => {
      e.stopPropagation();
      const wasOpen = item.classList.contains('flyout-open');
      _fecharTodos(mountPoint);
      if (!wasOpen) item.classList.add('flyout-open');
    });
  };

  // --Renderizar sidebar no #sidebar
  const render = (currentPage = '') => {
    const mountPoint = document.getElementById('sidebar');
    if (!mountPoint) return;

    const usuario  = ZyloAuth.getUsuario();
    const iniciais = ZyloFormat.iniciais(usuario?.nomeCompleto || '');
    const base     = ZyloAuth.baseUrl;
    const nav      = _getNav();
    const navHtml  = nav.map(g => _renderItem(g, currentPage)).join('');

    mountPoint.innerHTML = `
      <div class="sidebar-logo-wrap">
        <div class="sidebar-logo" title="Zylo ERP">
          <i class="bi bi-grid-3x3-gap-fill"></i>
        </div>
      </div>

      <nav class="sidebar-nav" aria-label="Navegação principal">
        ${navHtml}
      </nav>

      <div class="sidebar-footer">
        <div class="nav-rail-item">
          <div class="nav-rail-btn" title="${usuario?.nomeCompleto || 'Usuário'}">
            <div class="user-avatar-sm">${iniciais}</div>
          </div>
          <div class="nav-flyout flyout-user">
            <div class="flyout-user-header">
              <div class="flyout-user-avatar">${iniciais}</div>
              <div>
                <div class="flyout-user-nome">${usuario?.nomeCompleto || '—'}</div>
                <div class="flyout-user-perfil">${usuario?.perfil || ''}</div>
              </div>
            </div>
            <div class="flyout-divider"></div>
            <a href="${base}/pages/admin/usuarios-detalhe.html?id=${usuario?.codigoUsuario || ''}" class="flyout-link">
              <i class="bi bi-person"></i> Minha Conta
            </a>
            <a href="${base}/pages/admin/usuarios-detalhe.html?id=${usuario?.codigoUsuario || ''}#senha" class="flyout-link">
              <i class="bi bi-key"></i> Alterar Senha
            </a>
            <div class="flyout-divider"></div>
            <a href="#" class="flyout-link flyout-link-danger" data-action="logout">
              <i class="bi bi-box-arrow-right"></i> Sair
            </a>
          </div>
        </div>
      </div>`;

    // --Bind hover/clique em cada item
    mountPoint.querySelectorAll('.nav-rail-item').forEach(item => _bindItem(item, mountPoint));

    // --Logout
    mountPoint.querySelectorAll('[data-action="logout"]').forEach(el => {
      el.addEventListener('click', (e) => {
        e.preventDefault();
        if (confirm('Deseja realmente sair?')) ZyloAuth.logout();
      });
    });

    // --Fechar flyouts ao clicar fora
    document.addEventListener('click', (e) => {
      if (!mountPoint.contains(e.target)) _fecharTodos(mountPoint);
    });
  };

  return { render };

})();