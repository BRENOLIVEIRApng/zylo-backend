/* ─── Zylo ERP · main.js ────────────────────────────────────────────────────
   Carregado em TODAS as páginas protegidas (após auth.js e utils.js).

   ⚠️  NÃO adicionar aqui:
   - Toggle da sidebar  → sidebar.js já gerencia
   - Logout             → topbar.js e sidebar.js já gerenciam
   - Nav active state   → sidebar.js já gerencia via ZyloSidebar.render()
   - Dados do usuário   → sidebar.js e topbar.js já preenchem

   Este arquivo existe apenas para Bootstrap tooltips e qualquer
   inicialização global futura que não seja de layout.
────────────────────────────────────────────────────────────────────────────── */

document.addEventListener('DOMContentLoaded', () => {

  // ─── Bootstrap tooltips ──────────────────────────────────────────────────
  document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
    new bootstrap.Tooltip(el, { trigger: 'hover' });
  });

});