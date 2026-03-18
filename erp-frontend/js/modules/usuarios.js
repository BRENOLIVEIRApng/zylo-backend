/* ─── Zylo ERP · usuarios.js ────────────────────────────────────────────────── */

const ZyloUsuarios = (() => {

  // ─── Estado privado ────────────────────────────────────────────────────────
  let _usuarios = [];   // cache da listagem atual
  let _perfis   = [];   // cache de perfis para selects
  let _usuarioAtual = null; // objeto carregado na página de detalhe

  // ─── Helpers ──────────────────────────────────────────────────────────────
  const _iniciais = (nome) =>
    nome?.split(' ').filter(Boolean).slice(0, 2).map(n => n[0].toUpperCase()).join('') || '?';

  const _formatData = (val) => {
    if (!val) return '—';
    return new Date(val).toLocaleString('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  };

  const _formatDataCurta = (val) => {
    if (!val) return '—';
    return new Date(val).toLocaleDateString('pt-BR');
  };

  // ─── Força de senha ────────────────────────────────────────────────────────
  const _avaliarSenha = (senha, bars, hintId) => {
    const forca = senha.length === 0 ? 0
      : senha.length < 8 ? 1
      : senha.length < 12 && !/[^a-zA-Z0-9]/.test(senha) ? 2
      : 3;

    const classes = ['', 'fraca', 'media', 'forte'];
    bars.forEach((id, i) => {
      const el = document.getElementById(id);
      if (!el) return;
      el.className = 'senha-bar';
      if (i < forca) el.classList.add(classes[forca]);
    });

    const hints  = ['', 'Senha muito curta', 'Senha razoável — adicione caracteres especiais', 'Senha forte'];
    const hintEl = document.getElementById(hintId);
    if (hintEl) {
      hintEl.textContent = forca === 0 ? 'Mínimo 8 caracteres' : hints[forca];
      hintEl.style.color = forca === 1
        ? 'var(--accent-danger)'
        : forca === 2 ? 'var(--accent-warning)'
        : forca === 3 ? 'var(--accent-success)'
        : 'var(--text-muted)';
    }

    return forca;
  };

  // --Toggle visibilidade de senha genérico
  const _bindToggleSenha = (inputId, btnId) => {
    const btn = document.getElementById(btnId);
    if (!btn) return;
    btn.addEventListener('click', () => {
      const inp = document.getElementById(inputId);
      if (!inp) return;
      const visible = inp.type === 'text';
      inp.type = visible ? 'password' : 'text';
      const icon = btn.querySelector('i');
      if (icon) icon.className = `bi ${visible ? 'bi-eye' : 'bi-eye-slash'}`;
    });
  };

  // ─── Service (espelha UsuarioController) ───────────────────────────────────
  const Service = {
    listarAtivos:     ()   => ZyloHttp.get('/api/usuarios'),
    listarPorStatus:  (ativo) => ZyloHttp.get(`/api/usuarios/status/${ativo}`),
    listarPorPerfil:  (id) => ZyloHttp.get(`/api/usuarios/perfil/${id}`),
    buscarPorId:      (id) => ZyloHttp.get(`/api/usuarios/${id}`),
    me:               ()   => ZyloHttp.get('/api/usuarios/me'),

    criar:  (payload)          => ZyloHttp.post('/api/usuarios', payload),
    editar: (id, payload)      => ZyloHttp.put(`/api/usuarios/${id}`, payload),

    desativar:     (id) => ZyloHttp.patch(`/api/usuarios/${id}/desativar`, {}),
    ativar:        (id) => ZyloHttp.patch(`/api/usuarios/${id}/ativar`, {}),
    resetarSenha:  (id, novaSenha) => ZyloHttp.patch(`/api/usuarios/${id}/resetar-senha`, { novaSenha }),
    alterarSenha:  (senhaAtual, novaSenha) => ZyloHttp.patch('/api/usuarios/alterar-senha', { senhaAtual, novaSenha }),

    listarPerfis:  () => ZyloHttp.get('/api/perfis'),
    listarHistorico: (id) => ZyloHttp.get(`/api/usuarios/${id}/historico-acessos`),
  };

  // ─── UI Lista ──────────────────────────────────────────────────────────────
  const UILista = {

    // --Atualizar stat cards
    atualizarStats: (lista) => {
      const ativos   = lista.filter(u => u.ativo).length;
      const inativos = lista.filter(u => !u.ativo).length;
      const perfisDistintos = new Set(lista.map(u => u.codigoPerfil)).size;

      document.getElementById('statAtivos').textContent   = ativos;
      document.getElementById('statInativos').textContent = inativos;
      document.getElementById('statPerfis').textContent   = perfisDistintos;

      // --Último acesso
      const comAcesso = lista
        .filter(u => u.ultimoAcesso)
        .sort((a, b) => new Date(b.ultimoAcesso) - new Date(a.ultimoAcesso));
      if (comAcesso.length) {
        const u = comAcesso[0];
        document.getElementById('statUltimoAcesso').textContent = _formatDataCurta(u.ultimoAcesso);
        document.getElementById('statUltimoNome').innerHTML =
          `<i class="bi bi-person"></i> ${u.nomeCompleto.split(' ')[0]}`;
      }
    },

    // --Popular select de perfil no filtro
    popularFiltrosPerfil: (perfis) => {
      const sel = document.getElementById('filtroPerfil');
      if (!sel) return;
      perfis.forEach(p => {
        const opt = document.createElement('option');
        opt.value = p.codigoPerfil;
        opt.textContent = p.nomePerfil;
        sel.appendChild(opt);
      });
    },

    // --Popular selects de perfil nos modais
    popularSelectPerfil: (perfis, ...selectorIds) => {
      selectorIds.forEach(id => {
        const sel = document.getElementById(id);
        if (!sel) return;
        const valorAtual = sel.value;
        sel.innerHTML = '<option value="">Selecione o perfil...</option>';
        perfis.forEach(p => {
          const opt = document.createElement('option');
          opt.value = p.codigoPerfil;
          opt.textContent = p.nomePerfil;
          sel.appendChild(opt);
        });
        if (valorAtual) sel.value = valorAtual;
      });
    },

    // --Renderizar tabela
    renderTabela: (lista) => {
      const tbody = document.getElementById('corpoTabela');
      document.getElementById('rowLoading')?.remove();
      document.getElementById('labelTotal').textContent =
        `${lista.length} usuário${lista.length !== 1 ? 's' : ''} encontrado${lista.length !== 1 ? 's' : ''}`;

      if (!lista.length) {
        tbody.innerHTML = `
          <tr>
            <td colspan="6" style="text-align:center;padding:3rem 0;color:var(--text-muted);">
              <i class="bi bi-inbox" style="font-size:1.5rem;display:block;margin-bottom:.5rem;opacity:.4;"></i>
              Nenhum usuário encontrado
            </td>
          </tr>`;
        return;
      }

      tbody.innerHTML = lista.map(u => {
        const ini    = _iniciais(u.nomeCompleto);
        const inativo= !u.ativo;
        return `
        <tr>
          <td>
            <div class="usuario-info-cel">
              <div class="usuario-avatar${inativo ? ' inativo' : ''}">${ini}</div>
              <div>
                <div class="usuario-nome">${u.nomeCompleto}</div>
                <div class="usuario-email">${u.email}</div>
              </div>
            </div>
          </td>
          <td>
            <span class="perfil-badge">
              <i class="bi bi-shield"></i>${u.perfil || '—'}
            </span>
          </td>
          <td>
            <span class="badge-status ${u.ativo ? 'ativo' : 'suspenso'}">
              <i class="bi bi-circle-fill" style="font-size:.45rem;"></i>
              ${u.ativo ? 'Ativo' : 'Inativo'}
            </span>
          </td>
          <td class="ultimo-acesso">${_formatData(u.ultimoAcesso)}</td>
          <td class="ultimo-acesso">${_formatDataCurta(u.criadoEm)}</td>
          <td class="acoes-cel">
            <a href="usuarios-detalhe.html?id=${u.codigoUsuario}"
               class="btn btn-outline-secondary btn-sm" title="Ver detalhes">
              <i class="bi bi-eye"></i>
            </a>
            <button class="btn btn-outline-secondary btn-sm ms-1 btn-editar-lista"
              data-id="${u.codigoUsuario}" title="Editar">
              <i class="bi bi-pencil"></i>
            </button>
            <button class="btn btn-sm ms-1 btn-resetar-lista"
              data-id="${u.codigoUsuario}" data-nome="${u.nomeCompleto}"
              title="Resetar senha"
              style="border:1.5px solid var(--accent-warning);color:var(--accent-warning);background:transparent;border-radius:var(--radius-sm);padding:.25rem .5rem;">
              <i class="bi bi-key"></i>
            </button>
            ${u.ativo ? `
            <button class="btn btn-sm ms-1 btn-desativar-lista"
              data-id="${u.codigoUsuario}" data-nome="${u.nomeCompleto}"
              title="Desativar"
              style="border:1.5px solid var(--accent-danger);color:var(--accent-danger);background:transparent;border-radius:var(--radius-sm);padding:.25rem .5rem;">
              <i class="bi bi-person-slash"></i>
            </button>` : `
            <button class="btn btn-sm ms-1 btn-ativar-lista"
              data-id="${u.codigoUsuario}"
              title="Ativar"
              style="border:1.5px solid var(--accent-success);color:#00856A;background:transparent;border-radius:var(--radius-sm);padding:.25rem .5rem;">
              <i class="bi bi-person-check"></i>
            </button>`}
          </td>
        </tr>`;
      }).join('');

      // --Bind ações inline da tabela
      tbody.querySelectorAll('.btn-editar-lista').forEach(btn => {
        btn.addEventListener('click', () => Events._abrirModalEditar(parseInt(btn.dataset.id)));
      });
      tbody.querySelectorAll('.btn-resetar-lista').forEach(btn => {
        btn.addEventListener('click', () => Events._abrirModalReset(parseInt(btn.dataset.id), btn.dataset.nome));
      });
      tbody.querySelectorAll('.btn-desativar-lista').forEach(btn => {
        btn.addEventListener('click', () => Events._abrirModalDesativar(parseInt(btn.dataset.id), btn.dataset.nome));
      });
      tbody.querySelectorAll('.btn-ativar-lista').forEach(btn => {
        btn.addEventListener('click', () => Events._ativarUsuario(parseInt(btn.dataset.id)));
      });
    },

    // --Aplicar filtros locais
    aplicarFiltros: () => {
      const busca   = document.getElementById('campoBusca').value.toLowerCase();
      const perfil  = document.getElementById('filtroPerfil').value;
      const status  = document.getElementById('filtroStatus').value;

      let lista = [..._usuarios];

      if (status === 'ativos')   lista = lista.filter(u => u.ativo);
      if (status === 'inativos') lista = lista.filter(u => !u.ativo);
      if (perfil) lista = lista.filter(u => String(u.codigoPerfil) === perfil);
      if (busca)  lista = lista.filter(u =>
        u.nomeCompleto.toLowerCase().includes(busca) ||
        u.email.toLowerCase().includes(busca)
      );

      UILista.renderTabela(lista);
    },
  };

  // ─── UI Detalhe ────────────────────────────────────────────────────────────
  const UIDetalhe = {

    // --Renderizar card de dados do usuário
    renderDados: (u) => {
      const ini    = _iniciais(u.nomeCompleto);
      const inativo= !u.ativo;

      // Topbar breadcrumb
      document.getElementById('topbarNome').textContent    = u.nomeCompleto.split(' ')[0];
      document.getElementById('topbarSubNome').textContent = u.nomeCompleto;

      // Avatar
      const avatarEl = document.getElementById('detalheAvatar');
      avatarEl.textContent = ini;
      if (inativo) avatarEl.classList.add('inativo');

      // Status dot
      document.getElementById('detalheStatusDot').style.background =
        u.ativo ? 'var(--accent-success)' : 'var(--border-light)';

      // Nome / email
      document.getElementById('detalheNome').textContent  = u.nomeCompleto;
      document.getElementById('detalheEmail').textContent = u.email;

      // Status badge
      const badge = document.getElementById('detalheStatusBadge');
      badge.className = `badge-status ${u.ativo ? 'ativo' : 'suspenso'}`;
      badge.innerHTML = `<i class="bi bi-circle-fill" style="font-size:.45rem;"></i> ${u.ativo ? 'Ativo' : 'Inativo'}`;

      // Perfil
      document.getElementById('detalhePerfil').innerHTML =
        `<i class="bi bi-shield"></i> ${u.perfil || '—'}`;

      // Info grid
      document.getElementById('detalheCodigo').textContent      = `#${u.codigoUsuario}`;
      document.getElementById('detalheStatusTexto').innerHTML   =
        `<span class="badge-status ${u.ativo ? 'ativo' : 'suspenso'}" style="margin:0;">
           <i class="bi bi-circle-fill" style="font-size:.45rem;"></i>
           ${u.ativo ? 'Ativo' : 'Inativo'}
         </span>`;
      document.getElementById('detalheCriadoEm').textContent    = _formatData(u.criadoEm);
      document.getElementById('detalheUltimoAcesso').textContent= _formatData(u.ultimoAcesso);

      // Botão toggle status
      const btnToggle = document.getElementById('btnToggleStatusDetalhe');
      if (u.ativo) {
        btnToggle.style.borderColor = 'var(--accent-danger)';
        btnToggle.style.color       = 'var(--accent-danger)';
        btnToggle.innerHTML         = '<i class="bi bi-person-slash"></i> Desativar';
      } else {
        btnToggle.style.borderColor = 'var(--accent-success)';
        btnToggle.style.color       = '#00856A';
        btnToggle.innerHTML         = '<i class="bi bi-person-check"></i> Ativar';
      }

      document.getElementById('loadingDetalhe').style.display  = 'none';
      document.getElementById('conteudoDetalhe').style.display = 'block';
    },

    // --Renderizar histórico de acessos
    renderHistorico: (historico) => {
      const container = document.getElementById('historicoContainer');

      const falhas = historico.filter(h => !h.sucesso).length;
      const badgeFalhas = document.getElementById('badgeFalhas');
      if (falhas > 0) {
        badgeFalhas.style.display = 'inline-flex';
        document.getElementById('qtdFalhas').textContent = falhas;
      }

      if (!historico.length) {
        container.innerHTML = `
          <div style="text-align:center;padding:2.5rem 0;color:var(--text-muted);font-size:.85rem;">
            <i class="bi bi-clock-history" style="font-size:1.5rem;display:block;margin-bottom:.5rem;opacity:.4;"></i>
            Nenhum acesso registrado
          </div>`;
        return;
      }

      container.innerHTML = historico.map(h => `
        <div class="historico-item">
          <div class="historico-icon ${h.sucesso ? 'sucesso' : 'falha'}">
            <i class="bi ${h.sucesso ? 'bi-box-arrow-in-right' : 'bi-x-circle'}"></i>
          </div>
          <div class="historico-corpo">
            <div class="historico-titulo">${h.sucesso ? 'Login realizado com sucesso' : 'Tentativa de login falhou'}</div>
            <div class="historico-meta">
              <span><i class="bi bi-clock"></i> ${_formatData(h.dataHoraAcesso)}</span>
              ${h.ipAcesso ? `<span><i class="bi bi-geo"></i> ${h.ipAcesso}</span>` : ''}
              ${h.userAgent ? `<span title="${h.userAgent}" style="max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                <i class="bi bi-browser-chrome"></i> ${h.userAgent.split(' ')[0]}
              </span>` : ''}
            </div>
            ${!h.sucesso && h.motivoFalha ? `
              <div class="historico-motivo">
                <i class="bi bi-exclamation-circle"></i> ${h.motivoFalha}
              </div>` : ''}
          </div>
        </div>`).join('');
    },
  };

  // ─── Events ────────────────────────────────────────────────────────────────
  const Events = {

    // --Inicializar lista
    initLista: async () => {
      await Events._carregarPerfis();
      await Events._carregarUsuarios();
      Events._bindFiltros();
      Events._bindModalNovoUsuario();
      Events._bindModalResetarSenha();
      Events._bindModalDesativar();
      _bindToggleSenha('senhaUsuario', 'toggleSenhaModal');
      _bindToggleSenha('novaSenhaReset', 'toggleSenhaReset');

      // --Força de senha no modal novo
      document.getElementById('senhaUsuario')?.addEventListener('input', (e) => {
        _avaliarSenha(e.target.value, ['bar1', 'bar2', 'bar3'], 'senhaHint');
      });
      document.getElementById('novaSenhaReset')?.addEventListener('input', (e) => {
        _avaliarSenha(e.target.value, ['rbar1', 'rbar2', 'rbar3'], 'resetSenhaHint');
      });
    },

    // --Inicializar detalhe
    initDetalhe: async () => {
      const id = new URLSearchParams(window.location.search).get('id');
      if (!id) { window.location.href = 'usuarios-lista.html'; return; }

      await Events._carregarPerfis();
      await Events._carregarDetalhe(parseInt(id));
      Events._bindAcoesDetalhe();

      _bindToggleSenha('senhaAtual', 'toggleSenhaAtual');
      _bindToggleSenha('novaSenhaAlt', 'toggleNovaSenhaAlt');
      _bindToggleSenha('novaSenhaDetReset', 'toggleSenhaDetReset');

      document.getElementById('novaSenhaAlt')?.addEventListener('input', (e) => {
        _avaliarSenha(e.target.value, ['altbar1', 'altbar2', 'altbar3'], 'altSenhaHint');
      });
      document.getElementById('novaSenhaDetReset')?.addEventListener('input', (e) => {
        _avaliarSenha(e.target.value, ['detbar1', 'detbar2', 'detbar3'], 'detSenhaHint');
      });
    },

    // ─── Privados — lista ────────────────────────────────────────────────

    _carregarPerfis: async () => {
      try {
        _perfis = await Service.listarPerfis() || [];
        UILista.popularFiltrosPerfil(_perfis);
        UILista.popularSelectPerfil(_perfis, 'perfilUsuario', 'editarPerfil');
      } catch (_) { /* perfis opcionais no carregamento */ }
    },

    _carregarUsuarios: async () => {
      try {
        // --Carregar ativos + inativos em paralelo para ter cache completo
        const [ativos, inativos] = await Promise.all([
          Service.listarAtivos(),
          Service.listarPorStatus(false),
        ]);
        _usuarios = [...(ativos || []), ...(inativos || [])];
        UILista.atualizarStats(_usuarios);
        UILista.aplicarFiltros(); // aplica filtro padrão (ativos)
      } catch (err) {
        ZyloUI.showAlert('alertLista', 'Erro ao carregar usuários.', 'error');
      }
    },

    _bindFiltros: () => {
      ['campoBusca', 'filtroPerfil', 'filtroStatus'].forEach(id => {
        document.getElementById(id)?.addEventListener(
          id === 'campoBusca' ? 'input' : 'change',
          UILista.aplicarFiltros
        );
      });
      document.getElementById('btnLimparFiltros')?.addEventListener('click', () => {
        document.getElementById('campoBusca').value  = '';
        document.getElementById('filtroPerfil').value= '';
        document.getElementById('filtroStatus').value= 'ativos';
        UILista.aplicarFiltros();
      });
    },

    _bindModalNovoUsuario: () => {
      const modalEl = document.getElementById('modalUsuario');
      const modal   = new bootstrap.Modal(modalEl);

      document.getElementById('btnNovoUsuario').addEventListener('click', () => {
        // --Limpar formulário
        ['usuarioId','nomeCompleto','emailUsuario','senhaUsuario'].forEach(id => {
          const el = document.getElementById(id);
          if (el) el.value = '';
        });
        document.getElementById('perfilUsuario').value = '';
        document.getElementById('grupSenha').style.display = '';
        document.getElementById('modalUsuarioTitulo').innerHTML =
          '<i class="bi bi-person-plus me-2" style="color:var(--mint-dark);"></i>Novo Usuário';
        ZyloUI.clearAlert('alertModal');
        _avaliarSenha('', ['bar1','bar2','bar3'], 'senhaHint');
        modal.show();
      });

      document.getElementById('btnSalvarUsuario').addEventListener('click', async () => {
        await Events._salvarUsuario(modal);
      });

      modalEl.addEventListener('hidden.bs.modal', () => ZyloUI.clearAlert('alertModal'));
    },

    _abrirModalEditar: (id) => {
      const u = _usuarios.find(u => u.codigoUsuario === id);
      if (!u) return;

      document.getElementById('usuarioId').value    = u.codigoUsuario;
      document.getElementById('nomeCompleto').value = u.nomeCompleto;
      document.getElementById('emailUsuario').value = u.email;
      document.getElementById('perfilUsuario').value= u.codigoPerfil;
      document.getElementById('grupSenha').style.display = 'none'; // edição não requer senha
      document.getElementById('modalUsuarioTitulo').innerHTML =
        '<i class="bi bi-pencil me-2" style="color:var(--mint-dark);"></i>Editar Usuário';
      ZyloUI.clearAlert('alertModal');
      new bootstrap.Modal(document.getElementById('modalUsuario')).show();
    },

    _salvarUsuario: async (modal) => {
      const id    = document.getElementById('usuarioId').value;
      const nome  = document.getElementById('nomeCompleto').value.trim();
      const email = document.getElementById('emailUsuario').value.trim();
      const senha = document.getElementById('senhaUsuario').value;
      const perfil= document.getElementById('perfilUsuario').value;
      const btn   = document.getElementById('btnSalvarUsuario');

      if (!nome || nome.length < 3)  { ZyloUI.showAlert('alertModal', 'Nome deve ter no mínimo 3 caracteres.', 'error'); return; }
      if (!email || !email.includes('@')) { ZyloUI.showAlert('alertModal', 'Informe um email válido.', 'error'); return; }
      if (!id && senha.length < 8)   { ZyloUI.showAlert('alertModal', 'Senha deve ter no mínimo 8 caracteres.', 'error'); return; }
      if (!perfil)                   { ZyloUI.showAlert('alertModal', 'Selecione um perfil de acesso.', 'error'); return; }

      ZyloUI.btnLoading(btn, true, 'Salvando...');

      try {
        if (id) {
          const updated = await Service.editar(id, {
            nomeCompleto: nome, email, codigoPerfil: parseInt(perfil)
          });
          _usuarios = _usuarios.map(u => u.codigoUsuario === parseInt(id) ? { ...u, ...updated } : u);
        } else {
          const novo = await Service.criar({
            nomeCompleto: nome, email, senha, codigoPerfil: parseInt(perfil)
          });
          _usuarios.push(novo);
        }

        UILista.atualizarStats(_usuarios);
        UILista.aplicarFiltros();
        modal.hide();
      } catch (err) {
        ZyloUI.showAlert('alertModal', err.message || 'Erro ao salvar usuário.', 'error');
      } finally {
        ZyloUI.btnLoading(btn, false);
      }
    },

    _bindModalResetarSenha: () => {
      const modalEl = document.getElementById('modalResetarSenha');
      const modal   = new bootstrap.Modal(modalEl);

      // Estado do modal de reset (id atual)
      let _idResetAtual = null;

      document.getElementById('btnConfirmarReset').addEventListener('click', async () => {
        if (!_idResetAtual) return;
        const novaSenha = document.getElementById('novaSenhaReset').value;
        if (novaSenha.length < 8) {
          ZyloUI.showAlert('alertReset', 'Senha deve ter no mínimo 8 caracteres.', 'error'); return;
        }
        const btn = document.getElementById('btnConfirmarReset');
        ZyloUI.btnLoading(btn, true, 'Salvando...');
        try {
          await Service.resetarSenha(_idResetAtual, novaSenha);
          modal.hide();
          ZyloUI.showAlert('alertLista', 'Senha resetada com sucesso.', 'success');
        } catch (err) {
          ZyloUI.showAlert('alertReset', err.message || 'Erro ao resetar senha.', 'error');
        } finally {
          ZyloUI.btnLoading(btn, false);
        }
      });

      // --Setter chamado por _abrirModalReset
      Events._bindModalResetarSenha._abrir = (id, nome) => {
        _idResetAtual = id;
        document.getElementById('nomeResetarSenha').textContent = nome;
        document.getElementById('novaSenhaReset').value = '';
        _avaliarSenha('', ['rbar1','rbar2','rbar3'], 'resetSenhaHint');
        ZyloUI.clearAlert('alertReset');
        modal.show();
      };
    },

    _abrirModalReset: (id, nome) => {
      Events._bindModalResetarSenha._abrir?.(id, nome);
    },

    _bindModalDesativar: () => {
      const modalEl = document.getElementById('modalDesativar');
      const modal   = new bootstrap.Modal(modalEl);
      let _idDesativar = null;

      document.getElementById('btnConfirmarDesativar').addEventListener('click', async () => {
        if (!_idDesativar) return;
        const btn = document.getElementById('btnConfirmarDesativar');
        ZyloUI.btnLoading(btn, true, 'Desativando...');
        try {
          await Service.desativar(_idDesativar);
          _usuarios = _usuarios.map(u =>
            u.codigoUsuario === _idDesativar ? { ...u, ativo: false } : u
          );
          UILista.atualizarStats(_usuarios);
          UILista.aplicarFiltros();
          modal.hide();
        } catch (err) {
          ZyloUI.showAlert('alertLista', err.message || 'Erro ao desativar usuário.', 'error');
          modal.hide();
        } finally {
          ZyloUI.btnLoading(btn, false);
        }
      });

      Events._bindModalDesativar._abrir = (id, nome) => {
        _idDesativar = id;
        document.getElementById('nomeDesativar').textContent = nome;
        modal.show();
      };
    },

    _abrirModalDesativar: (id, nome) => {
      Events._bindModalDesativar._abrir?.(id, nome);
    },

    _ativarUsuario: async (id) => {
      try {
        await Service.ativar(id);
        _usuarios = _usuarios.map(u =>
          u.codigoUsuario === id ? { ...u, ativo: true } : u
        );
        UILista.atualizarStats(_usuarios);
        UILista.aplicarFiltros();
        ZyloUI.showAlert('alertLista', 'Usuário ativado com sucesso.', 'success');
      } catch (err) {
        ZyloUI.showAlert('alertLista', err.message || 'Erro ao ativar usuário.', 'error');
      }
    },

    // ─── Privados — detalhe ──────────────────────────────────────────────

    _carregarDetalhe: async (id) => {
      try {
        const [usuario, historico] = await Promise.all([
          Service.buscarPorId(id),
          Service.listarHistorico(id).catch(() => []),
        ]);
        _usuarioAtual = usuario;
        UIDetalhe.renderDados(usuario);
        UIDetalhe.renderHistorico(historico);

        // --Verificar se é o próprio usuário logado
        const me = ZyloAuth.getUsuario();
        if (me && me.codigoUsuario === usuario.codigoUsuario) {
          document.getElementById('cardAlterarSenha').style.display = 'block';
          UILista.popularSelectPerfil(_perfis, 'editarPerfil');
        }

        UILista.popularSelectPerfil(_perfis, 'editarPerfil');
        document.getElementById('editarId').value    = usuario.codigoUsuario;
        document.getElementById('editarNome').value  = usuario.nomeCompleto;
        document.getElementById('editarEmail').value = usuario.email;
        document.getElementById('editarPerfil').value= usuario.codigoPerfil;
      } catch (err) {
        document.getElementById('loadingDetalhe').innerHTML = `
          <div style="text-align:center;padding:4rem 0;color:var(--text-muted);">
            <i class="bi bi-exclamation-circle" style="font-size:2rem;display:block;margin-bottom:.75rem;opacity:.4;"></i>
            Usuário não encontrado
          </div>`;
        ZyloUI.showAlert('alertDetalhe', 'Erro ao carregar usuário.', 'error');
      }
    },

    _bindAcoesDetalhe: () => {
      // --Abrir modal editar
      document.getElementById('btnEditarDetalhe')?.addEventListener('click', () => {
        ZyloUI.clearAlert('alertEditarDetalhe');
        new bootstrap.Modal(document.getElementById('modalEditarUsuario')).show();
      });

      // --Salvar edição no detalhe
      document.getElementById('btnSalvarEdicaoDetalhe')?.addEventListener('click', async () => {
        if (!_usuarioAtual) return;
        const nome   = document.getElementById('editarNome').value.trim();
        const email  = document.getElementById('editarEmail').value.trim();
        const perfil = document.getElementById('editarPerfil').value;
        const btn    = document.getElementById('btnSalvarEdicaoDetalhe');

        if (!nome || !email || !perfil) {
          ZyloUI.showAlert('alertEditarDetalhe', 'Preencha todos os campos obrigatórios.', 'error'); return;
        }

        ZyloUI.btnLoading(btn, true, 'Salvando...');
        try {
          const updated = await Service.editar(_usuarioAtual.codigoUsuario, {
            nomeCompleto: nome, email, codigoPerfil: parseInt(perfil)
          });
          _usuarioAtual = { ..._usuarioAtual, ...updated };
          UIDetalhe.renderDados(_usuarioAtual);
          bootstrap.Modal.getInstance(document.getElementById('modalEditarUsuario'))?.hide();
          ZyloUI.showAlert('alertDetalhe', 'Usuário atualizado com sucesso.', 'success');
        } catch (err) {
          ZyloUI.showAlert('alertEditarDetalhe', err.message || 'Erro ao salvar.', 'error');
        } finally {
          ZyloUI.btnLoading(btn, false);
        }
      });

      // --Resetar senha no detalhe
      document.getElementById('btnResetarSenhaDetalhe')?.addEventListener('click', () => {
        document.getElementById('novaSenhaDetReset').value = '';
        _avaliarSenha('', ['detbar1','detbar2','detbar3'], 'detSenhaHint');
        ZyloUI.clearAlert('alertResetDetalhe');
        new bootstrap.Modal(document.getElementById('modalResetarSenhaDetalhe')).show();
      });

      document.getElementById('btnConfirmarResetDetalhe')?.addEventListener('click', async () => {
        if (!_usuarioAtual) return;
        const novaSenha = document.getElementById('novaSenhaDetReset').value;
        if (novaSenha.length < 8) {
          ZyloUI.showAlert('alertResetDetalhe', 'Senha deve ter no mínimo 8 caracteres.', 'error'); return;
        }
        const btn = document.getElementById('btnConfirmarResetDetalhe');
        ZyloUI.btnLoading(btn, true, 'Salvando...');
        try {
          await Service.resetarSenha(_usuarioAtual.codigoUsuario, novaSenha);
          bootstrap.Modal.getInstance(document.getElementById('modalResetarSenhaDetalhe'))?.hide();
          ZyloUI.showAlert('alertDetalhe', 'Senha resetada com sucesso.', 'success');
        } catch (err) {
          ZyloUI.showAlert('alertResetDetalhe', err.message || 'Erro ao resetar senha.', 'error');
        } finally {
          ZyloUI.btnLoading(btn, false);
        }
      });

      // --Toggle status (ativar / desativar)
      document.getElementById('btnToggleStatusDetalhe')?.addEventListener('click', () => {
        if (!_usuarioAtual) return;
        const ativo = _usuarioAtual.ativo;
        const btnConfirmar = document.getElementById('btnConfirmarToggleStatus');

        document.getElementById('tituloDesativarDetalhe').textContent =
          ativo ? 'Desativar Usuário' : 'Ativar Usuário';
        document.getElementById('textoDesativarDetalhe').innerHTML =
          `Deseja ${ativo ? 'desativar' : 'reativar'} o usuário <strong>${_usuarioAtual.nomeCompleto}</strong>?`;
        document.getElementById('alertDesativarInfo').style.display =
          ativo ? 'flex' : 'none';

        btnConfirmar.innerHTML = ativo
          ? '<i class="bi bi-person-slash"></i> Desativar'
          : '<i class="bi bi-person-check"></i> Ativar';
        btnConfirmar.style.background    = ativo ? 'var(--accent-danger)' : 'var(--mint)';
        btnConfirmar.style.borderColor   = ativo ? 'var(--accent-danger)' : 'var(--mint)';
        btnConfirmar.style.color         = ativo ? '#fff' : 'var(--text-on-mint)';

        // --Remover listener anterior e adicionar novo
        const novoBtn = btnConfirmar.cloneNode(true);
        btnConfirmar.replaceWith(novoBtn);
        novoBtn.addEventListener('click', async () => {
          ZyloUI.btnLoading(novoBtn, true, 'Salvando...');
          try {
            if (ativo) await Service.desativar(_usuarioAtual.codigoUsuario);
            else       await Service.ativar(_usuarioAtual.codigoUsuario);

            _usuarioAtual = { ..._usuarioAtual, ativo: !ativo };
            UIDetalhe.renderDados(_usuarioAtual);
            bootstrap.Modal.getInstance(document.getElementById('modalDesativarDetalhe'))?.hide();
            ZyloUI.showAlert('alertDetalhe',
              `Usuário ${!ativo ? 'ativado' : 'desativado'} com sucesso.`, 'success');
          } catch (err) {
            ZyloUI.showAlert('alertDetalhe', err.message || 'Erro ao alterar status.', 'error');
            bootstrap.Modal.getInstance(document.getElementById('modalDesativarDetalhe'))?.hide();
          } finally {
            ZyloUI.btnLoading(novoBtn, false);
          }
        });

        new bootstrap.Modal(document.getElementById('modalDesativarDetalhe')).show();
      });

      // --Formulário alterar própria senha
      document.getElementById('formAlterarSenha')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const senhaAtual = document.getElementById('senhaAtual').value;
        const novaSenha  = document.getElementById('novaSenhaAlt').value;
        const btn        = document.getElementById('btnAlterarSenha');

        if (!senhaAtual) { ZyloUI.showAlert('alertAlterarSenha', 'Informe a senha atual.', 'error'); return; }
        if (novaSenha.length < 8) { ZyloUI.showAlert('alertAlterarSenha', 'Nova senha deve ter no mínimo 8 caracteres.', 'error'); return; }

        ZyloUI.btnLoading(btn, true, 'Salvando...');
        try {
          await Service.alterarSenha(senhaAtual, novaSenha);
          document.getElementById('senhaAtual').value  = '';
          document.getElementById('novaSenhaAlt').value= '';
          _avaliarSenha('', ['altbar1','altbar2','altbar3'], 'altSenhaHint');
          ZyloUI.showAlert('alertAlterarSenha', 'Senha alterada com sucesso.', 'success');
        } catch (err) {
          ZyloUI.showAlert('alertAlterarSenha', err.message || 'Erro ao alterar senha.', 'error');
        } finally {
          ZyloUI.btnLoading(btn, false);
        }
      });
    },
  };

  return { Service, UILista, UIDetalhe, Events };
})();