import { urlDaApi } from "./api.js";

/**
 * Converte o corpoHtml (já sanitizado no backend pela política OWASP) em blocos do ArticleBody:
 *   { type: "h2", id, text } | { type: "p", text } | { type: "quote", text }
 *   { type: "image", src, alt, caption } | { type: "list", ordered, items: string[] }
 *   { type: "code", text } | { type: "hr" }
 * Só texto (textContent) é extraído — nada de HTML é injetado na página.
 * `imagens` (ConteudoDetalheResponse.texto.imagens) completa legendas ausentes no HTML.
 */
export function htmlParaBlocos(html, imagens = []) {
  if (!html || typeof DOMParser === "undefined") return [];
  const doc = new DOMParser().parseFromString(`<div>${html}</div>`, "text/html");
  const raiz = doc.body.firstElementChild;
  const legendaPorUrl = new Map((imagens || []).map((i) => [i.url, i.legenda]));
  const blocos = [];
  const idsUsados = new Set();
  let paragrafoSolto = [];

  const limpar = (t) => (t || "").replace(/\s+/g, " ").trim();

  function slug(texto) {
    const base = texto
      .normalize("NFD")
      .replace(/[̀-ͯ]/g, "")
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/^-+|-+$/g, "") || "secao";
    let id = base;
    for (let n = 2; idsUsados.has(id); n += 1) id = `${base}-${n}`;
    idsUsados.add(id);
    return id;
  }

  function imagem(img, legenda) {
    const src = img.getAttribute("src");
    if (!src) return;
    const alt = img.getAttribute("alt") || "";
    blocos.push({
      type: "image",
      src: urlDaApi(src),
      alt,
      caption: legenda || legendaPorUrl.get(src) || undefined,
    });
  }

  function descarregarSolto() {
    const texto = limpar(paragrafoSolto.join(" "));
    if (texto) blocos.push({ type: "p", text: texto });
    paragrafoSolto = [];
  }

  function visitar(no) {
    if (no.nodeType === Node.TEXT_NODE) {
      if (limpar(no.textContent)) paragrafoSolto.push(no.textContent);
      return;
    }
    if (no.nodeType !== Node.ELEMENT_NODE) return;
    const tag = no.tagName.toLowerCase();

    // Elementos inline soltos (b, i, a, span...) entram no parágrafo corrente.
    if (["b", "strong", "i", "em", "u", "a", "span", "code", "small", "sub", "sup", "s"].includes(tag)) {
      paragrafoSolto.push(no.textContent);
      return;
    }
    if (tag === "br") {
      descarregarSolto();
      return;
    }

    descarregarSolto();
    switch (tag) {
      case "h1":
      case "h2":
      case "h3":
      case "h4":
      case "h5":
      case "h6": {
        const text = limpar(no.textContent);
        if (text) blocos.push({ type: "h2", id: slug(text), text });
        break;
      }
      case "blockquote": {
        const text = limpar(no.textContent);
        if (text) blocos.push({ type: "quote", text });
        break;
      }
      case "ul":
      case "ol": {
        const items = Array.from(no.querySelectorAll(":scope > li")).map((li) => limpar(li.textContent)).filter(Boolean);
        if (items.length) blocos.push({ type: "list", ordered: tag === "ol", items });
        break;
      }
      case "pre": {
        const text = no.textContent.replace(/^\n+|\s+$/g, "");
        if (text) blocos.push({ type: "code", text });
        break;
      }
      case "hr":
        blocos.push({ type: "hr" });
        break;
      case "figure": {
        const img = no.querySelector("img");
        const cap = limpar(no.querySelector("figcaption")?.textContent);
        if (img) imagem(img, cap);
        else if (cap) blocos.push({ type: "p", text: cap });
        break;
      }
      case "img":
        imagem(no);
        break;
      case "p": {
        // Parágrafo pode conter imagens: separa texto e imagens na ordem em que aparecem.
        Array.from(no.childNodes).forEach((filho) => {
          if (filho.nodeType === Node.ELEMENT_NODE && filho.tagName.toLowerCase() === "img") {
            descarregarSolto();
            imagem(filho);
          } else if (filho.nodeType === Node.ELEMENT_NODE && filho.tagName.toLowerCase() === "br") {
            descarregarSolto();
          } else {
            paragrafoSolto.push(filho.textContent);
          }
        });
        descarregarSolto();
        break;
      }
      default:
        // div, section, table etc.: desce nos filhos.
        Array.from(no.childNodes).forEach(visitar);
        descarregarSolto();
    }
  }

  Array.from(raiz?.childNodes || []).forEach(visitar);
  descarregarSolto();
  return blocos;
}

/* ---------------- formatação compartilhada ---------------- */

const formatadorData = new Intl.DateTimeFormat("pt-BR", { day: "numeric", month: "short", year: "numeric" });

/** "2026-10-12" -> "12 out 2026" (sem fuso: a data do backend é LocalDate). */
export function formatarData(iso) {
  if (!iso) return "";
  const [ano, mes, dia] = String(iso).split("-").map(Number);
  if (!ano || !mes || !dia) return "";
  return formatadorData.format(new Date(ano, mes - 1, dia)).replace(/\./g, "").replace(/ de /g, " ");
}

export const ROTULO_TIPO = { texto: "Texto", pdf: "PDF" };

export const ROTULO_STATUS = { revisao: "Em revisão", devolvido: "Devolvido", publicado: "Publicado" };

export const ROTULO_PERFIL = { membro: "Membro", revisor: "Revisor", admin: "Administrador" };
