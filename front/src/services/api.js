export const FALLBACK_INSTITUICOES = [
  {
    id: "d9b2d6a4-4f81-42e3-99b8-1a2b3c4d5e6f",
    nome: "Universidade Federal do Maranhão",
    sigla: "UFMA",
  },
  {
    id: "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    nome: "Universidade Estadual do Maranhão",
    sigla: "UEMA",
  },
  {
    id: "f1e2d3c4-b5a6-9788-0c9d-8e7f6a5b4c3d",
    nome: "Instituto Federal do Maranhão",
    sigla: "IFMA",
  },
];

export async function fetchInstituicoes() {
  try {
    const res = await fetch("/auth/instituicoes", {
      headers: { Accept: "application/json" },
    });
    if (!res.ok) {
      return FALLBACK_INSTITUICOES;
    }
    const data = await res.json();
    if (Array.isArray(data) && data.length > 0) {
      return data;
    }
    return FALLBACK_INSTITUICOES;
  } catch (error) {
    console.warn("Falha de rede ao buscar instituições, usando fallback:", error);
    return FALLBACK_INSTITUICOES;
  }
}

export async function registerUsuario(payload) {
  const res = await fetch("/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(payload),
  });

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    if (res.status === 409) {
      throw new Error(data?.mensagem || "Email já cadastrado no sistema.");
    }
    if (data?.errors && Array.isArray(data.errors)) {
      const msgs = data.errors.map((e) => e.defaultMessage || "").filter(Boolean);
      throw new Error(msgs.join(", ") || "Dados inválidos.");
    }
    if (data?.mensagem) {
      throw new Error(data.mensagem);
    }
    throw new Error("Erro ao criar conta. Verifique os dados informados.");
  }

  return data;
}

export async function loginUsuario(payload) {
  const res = await fetch("/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(payload),
  });

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    if (res.status === 401 || res.status === 403) {
      throw new Error(data?.mensagem || "E-mail ou senha inválidos.");
    }
    if (data?.errors && Array.isArray(data.errors)) {
      const msgs = data.errors.map((e) => e.defaultMessage || "").filter(Boolean);
      throw new Error(msgs.join(", ") || "Dados inválidos.");
    }
    if (data?.mensagem) {
      throw new Error(data.mensagem);
    }
    throw new Error("Erro ao realizar login. Verifique suas credenciais.");
  }

  return data;
}

/**
 * Lista conteúdos publicados (GET /conteudos). Retorna o PagedModel do backend:
 * { content: [...], page: { size, number, totalElements, totalPages } }.
 */
export async function fetchConteudos({ q, tipo, tags, instituicaoId, sort = "data,desc", page = 0, size = 6 } = {}) {
  const params = new URLSearchParams();
  if (q && q.trim()) params.set("q", q.trim());
  if (tipo) params.set("tipo", tipo);
  (tags || []).filter(Boolean).forEach((t) => params.append("tags", t));
  if (instituicaoId) params.set("instituicaoId", instituicaoId);
  params.set("page", String(page));
  params.set("size", String(size));
  params.set("sort", sort);

  const res = await fetch(`/conteudos?${params.toString()}`, {
    headers: { Accept: "application/json" },
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error(data?.mensagem || "Não foi possível carregar os trabalhos.");
  }
  return {
    content: Array.isArray(data?.content) ? data.content : [],
    page: data?.page || { number: page, totalPages: 0, totalElements: 0, size },
  };
}

/** Lista as tags cadastradas (GET /tags). Em caso de falha retorna lista vazia. */
export async function fetchTags() {
  try {
    const res = await fetch("/tags", { headers: { Accept: "application/json" } });
    if (!res.ok) return [];
    const data = await res.json();
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.warn("Falha ao buscar tags:", error);
    return [];
  }
}
