/* ─── Zylo ERP · http.js ────────────────────────────────────────────────────
   Cliente HTTP com injeção automática de token JWT.
   Depende de: auth.js (ZyloAuth)
────────────────────────────────────────────────────────────────────────────── */

const ZyloHttp = (() => {

  // --URL do backend Spring Boot (NUNCA a porta do servidor de arquivos)
  const BASE = 'http://localhost:8080';

  const request = async (method, endpoint, body) => {
    let res;
    try {
      res = await fetch(`${BASE}${endpoint}`, {
        method,
        headers: ZyloAuth.authHeaders(),
        ...(body !== undefined ? { body: JSON.stringify(body) } : {})
      });
    } catch (networkErr) {
      // --Erro de rede (servidor offline, CORS etc.)
      throw new Error('Não foi possível conectar ao servidor. Verifique se o backend está rodando.');
    }

    // --Sessão expirada
    if (res.status === 401) {
      ZyloAuth.logout();
      return;
    }

    // --Sem conteúdo (204 — DELETE, PATCH sem retorno)
    if (res.status === 204) return null;

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      throw new Error(data?.mensagem || data?.message || `Erro ${res.status}`);
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