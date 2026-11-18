/**
 * Cliente HTTP do DTAComp.
 *
 * Todas as chamadas passam por `request()`, que:
 *  - prefixa a base da API (VITE_API_URL, padrão "/api" — o proxy do Vite remove o prefixo);
 *  - injeta o token JWT salvo na sessão;
 *  - interpreta o ErrorResponse do backend ({ status, error, message, timestamp });
 *  - em 401 com token, encerra a sessão e emite o evento "dtacomp:logout".
 */

export const API_BASE = (import.meta.env.VITE_API_URL ?? "/api").replace(/\/+$/, "");

const CHAVE_TOKEN = "dtacomp_token";
const CHAVE_USUARIO = "dtacomp_user";
export const EVENTO_LOGOUT = "dtacomp:logout";

/* ---------------- sessão ---------------- */

function lerStorage(chave) {
  try {
    return typeof window !== "undefined" ? localStorage.getItem(chave) : null;
  } catch {
    return null;
  }
}

export function getToken() {
  return lerStorage(CHAVE_TOKEN);
}

export function getUsuarioSalvo() {
  try {
    const salvo = lerStorage(CHAVE_USUARIO);
    return salvo ? JSON.parse(salvo) : null;
  } catch {
    return null;
  }
}

/** Salva token e dados do usuário. `token` ausente mantém o token atual. */
export function salvarSessao(usuario, token) {
  try {
    if (token) localStorage.setItem(CHAVE_TOKEN, token);
    if (usuario) {
      const dados = { ...usuario };
      delete dados.token;
      localStorage.setItem(CHAVE_USUARIO, JSON.stringify(dados));
    }
  } catch {
    // armazenamento indisponível: a sessão dura só enquanto a página estiver aberta
  }
}

export function limparSessao() {
  try {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
  } catch {
    // ignora
  }
}

/* ---------------- núcleo ---------------- */

export class ApiError extends Error {
  constructor(message, status, data) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.data = data;
  }
}

/**
 * Converte um caminho do backend (ex.: "/arquivos/imagens/x.png", "/conteudos/{id}/arquivo")
 * em URL utilizável pelo navegador (src de <img>, <object>, links de download).
 */
export function urlDaApi(caminho) {
  if (!caminho) return caminho;
  if (/^(https?:|data:|blob:)/i.test(caminho)) return caminho;
  if (API_BASE && caminho.startsWith(API_BASE + "/")) return caminho;
  return API_BASE + (caminho.startsWith("/") ? caminho : `/${caminho}`);
}

function montarQuery(params = {}) {
  const qs = new URLSearchParams();
  Object.entries(params).forEach(([chave, valor]) => {
    if (valor === undefined || valor === null || valor === "") return;
    if (Array.isArray(valor)) valor.filter(Boolean).forEach((v) => qs.append(chave, v));
    else qs.set(chave, String(valor));
  });
  const s = qs.toString();
  return s ? `?${s}` : "";
}

/**
 * @param {string} caminho  caminho do backend, ex.: "/conteudos"
 * @param {object} opcoes   { method, query, json, form, fallback }
 *   json: corpo serializado como application/json
 *   form: FormData (multipart)
 *   fallback: mensagem usada quando o backend não envia `message`
 */
export async function request(caminho, { method = "GET", query, json, form, fallback = "Não foi possível concluir a operação." } = {}) {
  const token = getToken();
  const headers = { Accept: "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;

  let body;
  if (json !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(json);
  } else if (form) {
    body = form;
  }

  let res;
  try {
    res = await fetch(`${API_BASE}${caminho}${montarQuery(query)}`, { method, headers, body });
  } catch {
    throw new ApiError("Não foi possível conectar ao servidor. Verifique sua conexão.", 0, null);
  }

  const texto = await res.text();
  let data = null;
  if (texto) {
    try {
      data = JSON.parse(texto);
    } catch {
      data = texto;
    }
  }

  if (!res.ok) {
    if (res.status === 401 && token) {
      limparSessao();
      if (typeof window !== "undefined") window.dispatchEvent(new Event(EVENTO_LOGOUT));
    }
    const mensagem = (data && typeof data === "object" && data.message) || fallback;
    throw new ApiError(mensagem, res.status, data);
  }

  return data;
}

function normalizarPagina(data, page, size) {
  return {
    content: Array.isArray(data?.content) ? data.content : [],
    page: data?.page || { number: page, totalPages: 0, totalElements: 0, size },
  };
}

function multipartComDados(dados, arquivo) {
  const form = new FormData();
  form.append("dados", new Blob([JSON.stringify(dados)], { type: "application/json" }));
  if (arquivo) form.append("arquivo", arquivo);
  return form;
}

/* ---------------- auth ---------------- */

export async function fetchInstituicoes() {
  const data = await request("/auth/instituicoes", { fallback: "Não foi possível carregar as instituições." });
  return Array.isArray(data) ? data : [];
}

export function registerUsuario(payload) {
  return request("/auth/register", {
    method: "POST",
    json: payload,
    fallback: "Erro ao criar conta. Verifique os dados informados.",
  });
}

export async function loginUsuario(payload) {
  try {
    return await request("/auth/login", {
      method: "POST",
      json: payload,
      fallback: "Erro ao realizar login. Verifique suas credenciais.",
    });
  } catch (e) {
    if (e.status === 401) throw new ApiError("E-mail ou senha inválidos.", 401, e.data);
    throw e;
  }
}

/** Dados do usuário autenticado (GET /auth/me). */
export function fetchMe() {
  return request("/auth/me", { fallback: "Sessão inválida." });
}

/* ---------------- conteúdos ---------------- */

/**
 * Lista conteúdos publicados (GET /conteudos). Retorna o PagedModel do backend:
 * { content: [...], page: { size, number, totalElements, totalPages } }.
 */
export async function fetchConteudos({ q, tipo, tags, instituicaoId, autorId, sort = "data,desc", page = 0, size = 6 } = {}) {
  const data = await request("/conteudos", {
    query: { q: q?.trim(), tipo, tags, instituicaoId, autorId, page, size, sort },
    fallback: "Não foi possível carregar os trabalhos.",
  });
  return normalizarPagina(data, page, size);
}

/** Detalhes completos de um conteúdo (GET /conteudos/{id}), incluindo pareceres quando permitido. */
export function fetchConteudoDetalhe(id) {
  return request(`/conteudos/${encodeURIComponent(id)}`, {
    fallback: "Não foi possível carregar os detalhes do trabalho.",
  });
}

export function criarConteudoTexto(payload) {
  return request("/conteudos/texto", {
    method: "POST",
    json: payload,
    fallback: "Não foi possível enviar o artigo para revisão.",
  });
}

export function atualizarConteudoTexto(id, payload) {
  return request(`/conteudos/${encodeURIComponent(id)}/texto`, {
    method: "PUT",
    json: payload,
    fallback: "Não foi possível atualizar o artigo.",
  });
}

export function criarConteudoPDF(dados, arquivo) {
  return request("/conteudos/pdf", {
    method: "POST",
    form: multipartComDados(dados, arquivo),
    fallback: "Não foi possível enviar o PDF para revisão.",
  });
}

export function atualizarConteudoPDF(id, dados, arquivo) {
  return request(`/conteudos/${encodeURIComponent(id)}/pdf`, {
    method: "PUT",
    form: multipartComDados(dados, arquivo),
    fallback: "Não foi possível atualizar o PDF.",
  });
}

export function excluirConteudo(id) {
  return request(`/conteudos/${encodeURIComponent(id)}`, {
    method: "DELETE",
    fallback: "Não foi possível excluir o trabalho.",
  });
}

/** Upload de imagem para o corpo do artigo (POST /arquivos/imagens). Retorna { url }. */
export function uploadImagem(arquivo) {
  const form = new FormData();
  form.append("arquivo", arquivo);
  return request("/arquivos/imagens", {
    method: "POST",
    form,
    fallback: "Falha ao enviar a imagem (PNG, JPEG ou WebP até 5 MB).",
  });
}

/* ---------------- tags ---------------- */

/** Lista as tags cadastradas (GET /tags). Em caso de falha retorna lista vazia. */
export async function fetchTags() {
  try {
    const data = await request("/tags");
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.warn("Falha ao buscar tags:", error);
    return [];
  }
}

export function criarTag(nome) {
  return request("/tags", { method: "POST", json: { nome }, fallback: "Não foi possível criar a tag." });
}

export function renomearTag(id, nome) {
  return request(`/tags/${encodeURIComponent(id)}`, { method: "PUT", json: { nome }, fallback: "Não foi possível renomear a tag." });
}

export function excluirTag(id) {
  return request(`/tags/${encodeURIComponent(id)}`, { method: "DELETE", fallback: "Não foi possível excluir a tag." });
}

/* ---------------- usuário autenticado ---------------- */

/** Conteúdos do próprio usuário (GET /usuarios/me/conteudos). status: revisao | devolvido | publicado. */
export async function fetchMeusConteudos({ status, page = 0, size = 20, sort = "data,desc" } = {}) {
  const data = await request("/usuarios/me/conteudos", {
    query: { status, page, size, sort },
    fallback: "Não foi possível carregar seus trabalhos.",
  });
  return normalizarPagina(data, page, size);
}

export function atualizarPerfil({ nome, idInstituicao }) {
  return request("/usuarios/me", {
    method: "PUT",
    json: { nome, idInstituicao },
    fallback: "Não foi possível atualizar o perfil.",
  });
}

export function alterarSenha({ senhaAtual, novaSenha }) {
  return request("/usuarios/me/senha", {
    method: "PUT",
    json: { senhaAtual, novaSenha },
    fallback: "Não foi possível alterar a senha.",
  });
}

export function exportarMeusDados() {
  return request("/usuarios/me/dados", { fallback: "Não foi possível exportar seus dados." });
}

export function excluirMinhaConta(senha) {
  return request("/usuarios/me", {
    method: "DELETE",
    json: { senha },
    fallback: "Não foi possível excluir a conta.",
  });
}

export function fetchPerfilPublico(id) {
  return request(`/usuarios/${encodeURIComponent(id)}/perfil`, { fallback: "Perfil não encontrado." });
}

/* ---------------- revisão ---------------- */

/** Fila de conteúdos aguardando revisão (exclui os do próprio revisor). */
export async function fetchPendentes({ page = 0, size = 50 } = {}) {
  const data = await request("/revisoes/pendentes", {
    query: { page, size },
    fallback: "Não foi possível carregar a fila de revisão.",
  });
  return normalizarPagina(data, page, size);
}

/** Revisões já registradas pelo revisor autenticado. */
export async function fetchMinhasRevisoes() {
  const data = await request("/revisoes/minhas", { fallback: "Não foi possível carregar as revisões concluídas." });
  return Array.isArray(data) ? data : [];
}

/** aprovado=true publica; aprovado=false devolve ao autor (parecer obrigatório). */
export function registrarRevisao({ idConteudo, aprovado, parecer }) {
  return request("/revisoes", {
    method: "POST",
    json: { idConteudo, aprovado, parecer: parecer?.trim() || null },
    fallback: "Não foi possível registrar a revisão.",
  });
}

/* ---------------- admin ---------------- */

/** perfil: "MEMBRO" | "REVISOR" | "ADMIN" (o backend usa o nome do enum). */
export async function adminListarUsuarios({ perfil, instituicaoId } = {}) {
  const data = await request("/admin/usuarios", {
    query: { perfil, instituicaoId },
    fallback: "Não foi possível carregar os usuários.",
  });
  return Array.isArray(data) ? data : [];
}

export function adminAlterarPerfil(id, perfil) {
  return request(`/admin/usuarios/${encodeURIComponent(id)}/perfil`, {
    method: "PUT",
    json: { perfil },
    fallback: "Não foi possível alterar o perfil.",
  });
}

export function adminAnonimizarUsuario(id) {
  return request(`/admin/usuarios/${encodeURIComponent(id)}`, {
    method: "DELETE",
    fallback: "Não foi possível anonimizar o usuário.",
  });
}

export function adminDespublicar(id, motivo) {
  return request(`/admin/conteudos/${encodeURIComponent(id)}/despublicar`, {
    method: "POST",
    json: { motivo },
    fallback: "Não foi possível despublicar o conteúdo.",
  });
}
