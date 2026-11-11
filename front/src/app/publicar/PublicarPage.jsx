import { useCallback, useEffect, useRef, useState } from "react";
import { SiteHeader } from "../../components/base/SiteHeader.jsx";
import { SiteFooter } from "../../components/base/SiteFooter.jsx";
import { Field } from "../../components/base/Field.jsx";
import { Button } from "../../components/base/Button.jsx";
import {
  PublishForm,
  EditorToolbar,
  WorkStatusCard,
  ReturnedNotice,
} from "../../components/publish/index.js";
import {
  fetchTags,
  fetchMeusConteudos,
  fetchConteudoDetalhe,
  uploadImagem,
  criarConteudoTexto,
  atualizarConteudoTexto,
  criarConteudoPDF,
  atualizarConteudoPDF,
} from "../../services/api.js";
import "./publicar.css";

const CHAVE_RASCUNHO = "dtacomp_rascunho_publicar";
const MAX_RESUMO = 300;
const MAX_PDF_BYTES = 20 * 1024 * 1024; // 20 MB (mesmo limite do backend)

const NAV_PUBLICAR = [
  { label: "Explorar", href: "/" },
  { label: "Meus trabalhos", href: "/dashboard" },
  { label: "Publicar", href: "/publicar", current: true },
];

const MODOS = [
  { id: "texto", label: "Escrever" },
  { id: "pdf", label: "Enviar PDF" },
];

const COMANDOS_EDITOR = [
  { id: "bold", label: <strong>B</strong>, ariaLabel: "Negrito" },
  { id: "italic", label: <em>I</em>, ariaLabel: "Itálico" },
  { id: "heading", label: "Título", ariaLabel: "Inserir título de seção" },
  { id: "list", label: "Lista", ariaLabel: "Inserir lista" },
  { id: "image", label: "Imagem", ariaLabel: "Inserir imagem" },
  { id: "quote", label: "Citação", ariaLabel: "Inserir citação" },
];

const AREAS_CONHECIMENTO = [
  "Ciência da Computação",
  "Engenharia de Software",
  "Inteligência Artificial",
  "Sistemas de Informação",
  "Ciência de Dados",
  "Redes e Sistemas Distribuídos",
  "Matemática Computacional",
  "Engenharia Elétrica",
];

const formatadorData = new Intl.DateTimeFormat("pt-BR", {
  day: "numeric",
  month: "short",
  year: "numeric",
});

function formatarData(iso) {
  if (!iso) return "[data]";
  const [ano, mes, dia] = iso.split("-").map(Number);
  if (!ano || !mes || !dia) return iso;
  return formatadorData
    .format(new Date(ano, mes - 1, dia))
    .replace(/\./g, "")
    .replace(/ de /g, " ");
}

function formatarTamanho(bytes) {
  if (!bytes && bytes !== 0) return "";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

/**
 * Funções auxiliares de escrita: manipulam o texto e a seleção do cursor no textarea
 * para inserir negrito, itálico, subtítulos, listas, imagens e citações.
 */
function aplicarComandoTexto(textoAtual, inicio, fim, comando, extra = {}) {
  const antes = textoAtual.slice(0, inicio);
  const selecao = textoAtual.slice(inicio, fim);
  const depois = textoAtual.slice(fim);
  const precisaQuebraAntes = antes.length > 0 && !antes.endsWith("\n");
  const prefixoLinha = precisaQuebraAntes ? "\n" : "";

  switch (comando) {
    case "bold": {
      const conteudo = selecao || "texto em negrito";
      const inserido = `**${conteudo}**`;
      return {
        novoTexto: antes + inserido + depois,
        novoInicio: inicio + 2,
        novoFim: inicio + 2 + conteudo.length,
      };
    }
    case "italic": {
      const conteudo = selecao || "texto em itálico";
      const inserido = `*${conteudo}*`;
      return {
        novoTexto: antes + inserido + depois,
        novoInicio: inicio + 1,
        novoFim: inicio + 1 + conteudo.length,
      };
    }
    case "heading": {
      const conteudo = selecao || "Título da seção";
      const inserido = `${prefixoLinha}## ${conteudo}\n`;
      const posInicio = inicio + prefixoLinha.length + 3;
      return {
        novoTexto: antes + inserido + depois,
        novoInicio: posInicio,
        novoFim: posInicio + conteudo.length,
      };
    }
    case "list": {
      const linhas = (selecao || "Item da lista")
        .split("\n")
        .map((l) => (l.trim().startsWith("- ") ? l : `- ${l}`))
        .join("\n");
      const inserido = `${prefixoLinha}${linhas}\n`;
      const posInicio = inicio + prefixoLinha.length + 2;
      return {
        novoTexto: antes + inserido + depois,
        novoInicio: posInicio,
        novoFim: inicio + prefixoLinha.length + linhas.length,
      };
    }
    case "quote": {
      const linhas = (selecao || "Trecho citado")
        .split("\n")
        .map((l) => (l.trim().startsWith("> ") ? l : `> ${l}`))
        .join("\n");
      const inserido = `${prefixoLinha}${linhas}\n`;
      const posInicio = inicio + prefixoLinha.length + 2;
      return {
        novoTexto: antes + inserido + depois,
        novoInicio: posInicio,
        novoFim: inicio + prefixoLinha.length + linhas.length,
      };
    }
    case "image": {
      const legenda = extra.legenda || selecao || "Legenda da imagem";
      const url = extra.url || "/arquivos/imagens/exemplo.png";
      const inserido = `${prefixoLinha}![${legenda}](${url})\n`;
      const posInicio = inicio + prefixoLinha.length + 2;
      return {
        novoTexto: antes + inserido + depois,
        novoInicio: posInicio,
        novoFim: posInicio + legenda.length,
      };
    }
    default:
      return { novoTexto: textoAtual, novoInicio: inicio, novoFim: fim };
  }
}

function escaparHtml(str) {
  return str
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function formatarInline(linha) {
  return escaparHtml(linha)
    .replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.+?)\*/g, "<em>$1</em>");
}

/**
 * Converte o texto do editor (com marcações da barra de ferramentas ou HTML)
 * em HTML semântico compatível com a política OWASP do backend (PublicacaoService).
 */
function converterTextoParaHtml(texto) {
  const bruto = (texto || "").trim();
  if (!bruto) return "";

  // Se o autor já escreveu tags HTML de bloco diretamente, preserva
  if (/^<(p|h[1-6]|blockquote|ul|ol|figure|pre)\b/i.test(bruto)) {
    return bruto;
  }

  const linhas = bruto.split(/\r?\n/);
  const blocos = [];
  let itensLista = [];
  let linhasCitacao = [];
  let linhasParagrafo = [];

  function fecharParagrafo() {
    if (linhasParagrafo.length > 0) {
      blocos.push(`<p>${linhasParagrafo.map(formatarInline).join("<br />")}</p>`);
      linhasParagrafo = [];
    }
  }

  function fecharLista() {
    if (itensLista.length > 0) {
      blocos.push(
        `<ul>${itensLista.map((item) => `<li>${formatarInline(item)}</li>`).join("")}</ul>`
      );
      itensLista = [];
    }
  }

  function fecharCitacao() {
    if (linhasCitacao.length > 0) {
      blocos.push(
        `<blockquote>${linhasCitacao.map(formatarInline).join(" ")}</blockquote>`
      );
      linhasCitacao = [];
    }
  }

  for (const linhaOriginal of linhas) {
    const linha = linhaOriginal.trim();

    if (!linha) {
      fecharParagrafo();
      fecharLista();
      fecharCitacao();
      continue;
    }

    const matchImagem = linha.match(/^!\[([^\]]*)\]\(([^)]+)\)$/);
    if (matchImagem) {
      fecharParagrafo();
      fecharLista();
      fecharCitacao();
      const legenda = escaparHtml(matchImagem[1].trim());
      const url = escaparHtml(matchImagem[2].trim());
      blocos.push(
        legenda
          ? `<figure><img src="${url}" alt="${legenda}" /><figcaption>${legenda}</figcaption></figure>`
          : `<figure><img src="${url}" alt="Imagem do artigo" /></figure>`
      );
      continue;
    }

    if (linha.startsWith("## ")) {
      fecharParagrafo();
      fecharLista();
      fecharCitacao();
      blocos.push(`<h2>${formatarInline(linha.slice(3).trim())}</h2>`);
      continue;
    }

    if (linha.startsWith("# ")) {
      fecharParagrafo();
      fecharLista();
      fecharCitacao();
      blocos.push(`<h2>${formatarInline(linha.slice(2).trim())}</h2>`);
      continue;
    }

    if (linha.startsWith("- ") || linha.startsWith("* ")) {
      fecharParagrafo();
      fecharCitacao();
      itensLista.push(linha.slice(2).trim());
      continue;
    }

    if (linha.startsWith("> ")) {
      fecharParagrafo();
      fecharLista();
      linhasCitacao.push(linha.slice(2).trim());
      continue;
    }

    fecharLista();
    fecharCitacao();
    linhasParagrafo.push(linha);
  }

  fecharParagrafo();
  fecharLista();
  fecharCitacao();

  return blocos.join("\n");
}

/** Converte o corpoHtml vindo do backend de volta para o formato legível no editor. */
function htmlParaTextoEditor(html = "") {
  if (!html) return "";
  return html
    .replace(/<figure>\s*<img[^>]*src="([^"]+)"[^>]*alt="([^"]*)"[^>]*\/?>\s*(?:<figcaption>([\s\S]*?)<\/figcaption>)?\s*<\/figure>/gi, (_, src, alt, cap) => `\n![${cap || alt || ""}](${src})\n`)
    .replace(/<h[1-6][^>]*>([\s\S]*?)<\/h[1-6]>/gi, "\n## $1\n")
    .replace(/<blockquote[^>]*>([\s\S]*?)<\/blockquote>/gi, "\n> $1\n")
    .replace(/<li[^>]*>([\s\S]*?)<\/li>/gi, "- $1\n")
    .replace(/<\/?(ul|ol)[^>]*>/gi, "\n")
    .replace(/<strong[^>]*>([\s\S]*?)<\/strong>/gi, "**$1**")
    .replace(/<b[^>]*>([\s\S]*?)<\/b>/gi, "**$1**")
    .replace(/<em[^>]*>([\s\S]*?)<\/em>/gi, "*$1*")
    .replace(/<i[^>]*>([\s\S]*?)<\/i>/gi, "*$1*")
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<\/p>\s*<p[^>]*>/gi, "\n\n")
    .replace(/<\/?p[^>]*>/gi, "")
    .replace(/&quot;/g, '"')
    .replace(/&gt;/g, ">")
    .replace(/&lt;/g, "<")
    .replace(/&amp;/g, "&")
    .trim();
}

/**
 * Extrai apenas imagens válidas do backend (/arquivos/imagens/<uuid>.<ext>)
 * presentes no texto para enviar no campo `imagens` de ConteudoTextoRequest.
 */
function extrairImagensValidas(texto) {
  const regex = /!\[([^\]]*)\]\((\/arquivos\/imagens\/[0-9a-fA-F-]{36}\.(?:png|jpg|jpeg|webp))\)/gi;
  const encontradas = [];
  const vistas = new Set();
  let match;
  while ((match = regex.exec(texto)) !== null) {
    const legenda = match[1].trim() || null;
    const url = match[2].trim();
    if (!vistas.has(url)) {
      vistas.add(url);
      encontradas.push({ url, legenda });
    }
  }
  return encontradas;
}

/** Valida no cliente se o arquivo tem cabeçalho %PDF- e respeita o limite de 20 MB. */
async function validarArquivoPdfCliente(arquivo) {
  if (!arquivo) return "Selecione um arquivo PDF para enviar.";
  if (arquivo.size === 0) return "O arquivo PDF selecionado está vazio.";
  if (arquivo.size > MAX_PDF_BYTES) {
    return "O PDF excede o tamanho máximo de 20 MB.";
  }
  const nomeValido = arquivo.name?.toLowerCase().endsWith(".pdf");
  const tipoValido = arquivo.type === "application/pdf" || nomeValido;
  if (!tipoValido) {
    return "O arquivo deve ser um PDF (.pdf).";
  }
  try {
    const buffer = await arquivo.slice(0, 5).arrayBuffer();
    const cabecalho = new TextDecoder("ascii").decode(buffer);
    if (cabecalho !== "%PDF-") {
      return "O conteúdo do arquivo não é um PDF válido.";
    }
  } catch {
    // Caso o ambiente não suporte slice/arrayBuffer, o backend fará a validação final
  }
  return null;
}

function lerRascunhoInicial() {
  if (typeof window === "undefined") return {};
  try {
    const salvo = localStorage.getItem(CHAVE_RASCUNHO);
    return salvo ? JSON.parse(salvo) || {} : {};
  } catch {
    return {};
  }
}

export function PublicarPage({ user = null, onSignIn }) {
  const textareaRef = useRef(null);
  const inputImagemRef = useRef(null);
  const inputPdfRef = useRef(null);

  const [rascunhoInicial] = useState(lerRascunhoInicial);

  // Modo: "texto" (Escrever) ou "pdf" (Enviar PDF)
  const [modo, setModo] = useState(() =>
    rascunhoInicial.modo === "pdf" ? "pdf" : "texto"
  );

  // Campos do formulário (compartilhados entre os dois modos; só a seção "Texto" muda)
  const [titulo, setTitulo] = useState(() => rascunhoInicial.titulo || "");
  const [resumo, setResumo] = useState(() => rascunhoInicial.resumo || "");
  const [texto, setTexto] = useState(() => rascunhoInicial.texto || "");
  const [arquivoPdf, setArquivoPdf] = useState(null);
  const [tagsInput, setTagsInput] = useState(() => rascunhoInicial.tagsInput || "");
  const [area, setArea] = useState(() => rascunhoInicial.area || "");

  // Estado de edição quando o autor clica em "Ver relatório e corrigir"
  const [trabalhoEmCorrecao, setTrabalhoEmCorrecao] = useState(null);

  // Dados carregados do backend
  const [tagsCadastradas, setTagsCadastradas] = useState([]);
  const [meusTrabalhos, setMeusTrabalhos] = useState([]);

  // Estados de UI
  const [erros, setErros] = useState({});
  const [mensagemSucesso, setMensagemSucesso] = useState("");
  const [erroServidor, setErroServidor] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [enviandoImagem, setEnviandoImagem] = useState(false);
  const [arrastandoPdf, setArrastandoPdf] = useState(false);

  const carregarMeusTrabalhos = useCallback(async () => {
    const token = typeof window !== "undefined" ? localStorage.getItem("dtacomp_token") : null;
    if (!token) return;
    try {
      const res = await fetchMeusConteudos({ size: 10, sort: "data,desc" });
      setMeusTrabalhos(res.content || []);
    } catch {
      // Mantém estado atual caso não autenticado ou offline
    }
  }, []);

  useEffect(() => {
    let ativo = true;
    fetchTags().then((lista) => {
      if (ativo) setTagsCadastradas(lista);
    });
    const token = typeof window !== "undefined" ? localStorage.getItem("dtacomp_token") : null;
    if (token) {
      fetchMeusConteudos({ size: 10, sort: "data,desc" })
        .then((res) => {
          if (ativo) setMeusTrabalhos(res.content || []);
        })
        .catch(() => {});
    }
    return () => {
      ativo = false;
    };
  }, [user]);

  function limparFeedback() {
    if (mensagemSucesso) setMensagemSucesso("");
    if (erroServidor) setErroServidor("");
  }

  /** Executa os comandos da barra de ferramentas (B, I, Título, Lista, Imagem, Citação). */
  function executarComandoEditor(comandoId) {
    limparFeedback();
    if (comandoId === "image") {
      if (inputImagemRef.current) {
        inputImagemRef.current.value = "";
        inputImagemRef.current.click();
      }
      return;
    }

    const el = textareaRef.current;
    const inicio = el ? el.selectionStart : texto.length;
    const fim = el ? el.selectionEnd : texto.length;

    const { novoTexto, novoInicio, novoFim } = aplicarComandoTexto(
      texto,
      inicio,
      fim,
      comandoId
    );

    setTexto(novoTexto);
    if (erros.texto) setErros((prev) => ({ ...prev, texto: undefined }));

    requestAnimationFrame(() => {
      if (textareaRef.current) {
        textareaRef.current.focus();
        textareaRef.current.setSelectionRange(novoInicio, novoFim);
      }
    });
  }

  /** Trata atalhos de teclado no editor (Ctrl+B para negrito, Ctrl+I para itálico). */
  function handleEditorKeyDown(e) {
    if ((e.ctrlKey || e.metaKey) && !e.shiftKey && !e.altKey) {
      const tecla = e.key.toLowerCase();
      if (tecla === "b") {
        e.preventDefault();
        executarComandoEditor("bold");
      } else if (tecla === "i") {
        e.preventDefault();
        executarComandoEditor("italic");
      }
    }
  }

  /** Upload de imagem via POST /arquivos/imagens ao clicar no botão "Imagem" da toolbar. */
  async function handleSelecionarImagem(e) {
    const arquivo = e.target.files?.[0];
    if (!arquivo) return;

    limparFeedback();
    setEnviandoImagem(true);

    const el = textareaRef.current;
    const inicio = el ? el.selectionStart : texto.length;
    const fim = el ? el.selectionEnd : texto.length;
    const legendaPadrao = arquivo.name.replace(/\.[^.]+$/, "") || "Figura";

    try {
      const resposta = await uploadImagem(arquivo);
      const { novoTexto, novoInicio, novoFim } = aplicarComandoTexto(
        texto,
        inicio,
        fim,
        "image",
        { url: resposta.url, legenda: legendaPadrao }
      );
      setTexto(novoTexto);
      requestAnimationFrame(() => {
        if (textareaRef.current) {
          textareaRef.current.focus();
          textareaRef.current.setSelectionRange(novoInicio, novoFim);
        }
      });
    } catch (err) {
      // Se não estiver autenticado no backend ou falhar, insere o marcador no texto e avisa
      const { novoTexto, novoInicio, novoFim } = aplicarComandoTexto(
        texto,
        inicio,
        fim,
        "image",
        { url: `/arquivos/imagens/${arquivo.name}`, legenda: legendaPadrao }
      );
      setTexto(novoTexto);
      setErroServidor(
        err.message || "Marcador de imagem inserido. Faça login para enviar a imagem ao servidor."
      );
      requestAnimationFrame(() => {
        if (textareaRef.current) {
          textareaRef.current.focus();
          textareaRef.current.setSelectionRange(novoInicio, novoFim);
        }
      });
    } finally {
      setEnviandoImagem(false);
    }
  }

  /** Seleção ou drag-and-drop do arquivo PDF quando o modo é "Enviar PDF". */
  async function handleSelecionarPdf(arquivo) {
    limparFeedback();
    if (!arquivo) return;
    const erroPdf = await validarArquivoPdfCliente(arquivo);
    if (erroPdf) {
      setArquivoPdf(null);
      setErros((prev) => ({ ...prev, arquivo: erroPdf }));
      return;
    }
    setArquivoPdf(arquivo);
    setErros((prev) => ({ ...prev, arquivo: undefined }));
  }

  function salvarRascunho() {
    limparFeedback();
    try {
      localStorage.setItem(
        CHAVE_RASCUNHO,
        JSON.stringify({
          modo,
          titulo,
          resumo,
          texto,
          tagsInput,
          area,
          salvoEm: new Date().toISOString(),
        })
      );
      setMensagemSucesso("Rascunho salvo com sucesso neste navegador.");
    } catch {
      setErroServidor("Não foi possível salvar o rascunho localmente.");
    }
  }

  /** Carrega um trabalho devolvido para correção e reenvio (PUT /conteudos/{id}/texto ou /pdf). */
  async function iniciarCorrecao(trabalhoResumo) {
    limparFeedback();
    setErros({});
    try {
      const detalhe = await fetchConteudoDetalhe(trabalhoResumo.id);
      setTrabalhoEmCorrecao(detalhe);
      setModo(detalhe.tipo === "pdf" ? "pdf" : "texto");
      setTitulo(detalhe.titulo || "");
      setResumo(detalhe.resumo || "");
      setTagsInput((detalhe.tags || []).join(", "));
      if (detalhe.texto?.corpoHtml) {
        setTexto(htmlParaTextoEditor(detalhe.texto.corpoHtml));
      }
      setArquivoPdf(null);
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (err) {
      setErroServidor(err.message || "Não foi possível carregar o relatório de revisão.");
    }
  }

  function cancelarCorrecao() {
    setTrabalhoEmCorrecao(null);
    setTitulo("");
    setResumo("");
    setTexto("");
    setArquivoPdf(null);
    setTagsInput("");
    setArea("");
    setErros({});
    limparFeedback();
  }

  function obterEmailAutor() {
    if (user?.email) return user.email;
    try {
      const salvo = JSON.parse(localStorage.getItem("dtacomp_user") || "null");
      if (salvo?.email) return salvo.email;
    } catch {
      // ignora
    }
    return null;
  }

  function montarListaTags() {
    const brutas = tagsInput
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean);
    if (area && !brutas.some((t) => t.toLowerCase() === area.toLowerCase())) {
      brutas.push(area);
    }
    return brutas.slice(0, 20);
  }

  async function validarFormulario() {
    const novosErros = {};

    if (!titulo.trim()) {
      novosErros.titulo = "Título é obrigatório.";
    } else if (titulo.trim().length > 255) {
      novosErros.titulo = "Título deve ter no máximo 255 caracteres.";
    }

    if (resumo.length > MAX_RESUMO) {
      novosErros.resumo = `Resumo deve ter no máximo ${MAX_RESUMO} caracteres.`;
    }

    if (modo === "texto") {
      const html = converterTextoParaHtml(texto);
      if (!html) {
        novosErros.texto = "Corpo do texto é obrigatório.";
      }
    } else {
      // No reenvio de PDF devolvido, o arquivo só é obrigatório se o usuário quiser substituí-lo
      const exigeArquivo = !trabalhoEmCorrecao || trabalhoEmCorrecao.tipo !== "pdf";
      if (exigeArquivo || arquivoPdf) {
        const erroPdf = await validarArquivoPdfCliente(arquivoPdf);
        if (erroPdf) {
          novosErros.arquivo = erroPdf;
        }
      }
    }

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    limparFeedback();

    if (!(await validarFormulario())) {
      return;
    }

    const emailAutor = obterEmailAutor();
    if (!emailAutor) {
      setErroServidor("Você precisa entrar na sua conta antes de enviar um trabalho para revisão.");
      return;
    }

    const autores =
      trabalhoEmCorrecao?.autores?.length > 0
        ? Array.from(
            new Set([
              ...trabalhoEmCorrecao.autores.map((a) => a.email).filter(Boolean),
              emailAutor,
            ])
          )
        : [emailAutor];

    const todasTags = montarListaTags();
    // Filtra tags existentes quando o backend já retornou a lista, evitando erro de tag não cadastrada
    const nomesCadastrados = new Map(
      tagsCadastradas.map((t) => [t.nome.toLowerCase(), t.nome])
    );
    const tagsValidas =
      nomesCadastrados.size > 0
        ? todasTags
            .map((t) => nomesCadastrados.get(t.toLowerCase()))
            .filter(Boolean)
        : todasTags;

    setEnviando(true);

    try {
      if (modo === "texto") {
        const corpoHtml = converterTextoParaHtml(texto);
        const imagens = extrairImagensValidas(texto);
        const payload = {
          titulo: titulo.trim(),
          resumo: resumo.trim() || null,
          corpoHtml,
          tags: tagsValidas,
          autores,
          imagens,
        };

        try {
          if (trabalhoEmCorrecao) {
            await atualizarConteudoTexto(trabalhoEmCorrecao.id, payload);
          } else {
            await criarConteudoTexto(payload);
          }
        } catch (err) {
          // Caso uma tag digitada não exista no banco, reenvia sem as tags inexistentes
          if (err.message?.includes("Tags inexistentes") && payload.tags.length > 0) {
            const payloadSemTags = { ...payload, tags: [] };
            if (trabalhoEmCorrecao) {
              await atualizarConteudoTexto(trabalhoEmCorrecao.id, payloadSemTags);
            } else {
              await criarConteudoTexto(payloadSemTags);
            }
          } else {
            throw err;
          }
        }
      } else {
        const dadosPdf = {
          titulo: titulo.trim(),
          resumo: resumo.trim() || null,
          tags: tagsValidas,
          autores,
        };

        try {
          if (trabalhoEmCorrecao) {
            await atualizarConteudoPDF(trabalhoEmCorrecao.id, dadosPdf, arquivoPdf);
          } else {
            await criarConteudoPDF(dadosPdf, arquivoPdf);
          }
        } catch (err) {
          if (err.message?.includes("Tags inexistentes") && dadosPdf.tags.length > 0) {
            const dadosSemTags = { ...dadosPdf, tags: [] };
            if (trabalhoEmCorrecao) {
              await atualizarConteudoPDF(trabalhoEmCorrecao.id, dadosSemTags, arquivoPdf);
            } else {
              await criarConteudoPDF(dadosSemTags, arquivoPdf);
            }
          } else {
            throw err;
          }
        }
      }

      localStorage.removeItem(CHAVE_RASCUNHO);
      setTrabalhoEmCorrecao(null);
      setTitulo("");
      setResumo("");
      setTexto("");
      setArquivoPdf(null);
      setTagsInput("");
      setArea("");
      setErros({});
      setMensagemSucesso(
        trabalhoEmCorrecao
          ? "Trabalho corrigido e reenviado para revisão com sucesso!"
          : "Trabalho enviado para revisão com sucesso!"
      );
      await carregarMeusTrabalhos();
    } catch (err) {
      setErroServidor(err.message || "Erro ao enviar o trabalho. Tente novamente.");
    } finally {
      setEnviando(false);
    }
  }

  const emAndamento = meusTrabalhos.filter(
    (t) => t.status === "revisao" || t.status === "publicado"
  );
  const devolvidos = meusTrabalhos.filter((t) => t.status === "devolvido");
  const ultimoParecer =
    trabalhoEmCorrecao?.revisoes?.[trabalhoEmCorrecao.revisoes.length - 1]?.parecer;

  // Opções de área do conhecimento combinadas com as tags do backend
  const opcoesArea = Array.from(
    new Set([
      ...AREAS_CONHECIMENTO,
      ...tagsCadastradas.map((t) => t.nome).filter(Boolean),
    ])
  );

  return (
    <div className="dta dta-publicar">
      <SiteHeader
        user={user || { nome: "[Nome do membro]" }}
        nav={NAV_PUBLICAR}
        onSignIn={onSignIn}
      />

      <main className="dta-wrap">
        <div className="dta-publicar__grid">
          <PublishForm
            ariaLabel="Publicar um trabalho"
            kicker={trabalhoEmCorrecao ? "CORRIGIR CONTEÚDO" : "NOVO CONTEÚDO"}
            title="Publicar um trabalho"
            description="Escreva direto aqui ou envie um PDF. Todo conteúdo passa por revisão antes de ir ao ar."
            modes={MODOS}
            modesLabel="Modo de publicação"
            mode={modo}
            onModeChange={(novoModo) => {
              // Na correção de trabalho devolvido, o backend exige manter o mesmo tipo
              if (trabalhoEmCorrecao && trabalhoEmCorrecao.tipo !== novoModo) {
                setErroServidor(
                  `Este trabalho foi submetido como ${trabalhoEmCorrecao.tipo.toUpperCase()} e deve ser reenviado no mesmo formato.`
                );
                return;
              }
              limparFeedback();
              setModo(novoModo);
            }}
            onSubmit={handleSubmit}
            submitLabel={
              enviando
                ? "Enviando..."
                : trabalhoEmCorrecao
                  ? "Reenviar para revisão"
                  : "Enviar para revisão"
            }
            secondaryActions={[
              { label: "Salvar rascunho", onClick: salvarRascunho },
              ...(trabalhoEmCorrecao
                ? [{ label: "Cancelar correção", onClick: cancelarCorrecao }]
                : []),
            ]}
            note="Conteúdo impróprio, pejorativo ou discriminatório é recusado na revisão. Seus dados são tratados conforme a LGPD."
          >
            {trabalhoEmCorrecao && (
              <div className="dta-publicar__alert dta-publicar__alert--parecer" role="status">
                <strong>Corrigindo: {trabalhoEmCorrecao.titulo}</strong>
                {ultimoParecer && (
                  <p style={{ margin: "6px 0 0" }}>
                    <strong>Relatório do revisor:</strong> {ultimoParecer}
                  </p>
                )}
              </div>
            )}

            {erroServidor && (
              <div className="dta-publicar__alert dta-publicar__alert--error" role="alert">
                {erroServidor}
              </div>
            )}

            {mensagemSucesso && (
              <div className="dta-publicar__alert dta-publicar__alert--success" role="status">
                {mensagemSucesso}
              </div>
            )}

            {/* Título */}
            <Field label="Título" id="pub-titulo" error={erros.titulo}>
              {(fieldProps) => (
                <input
                  type="text"
                  value={titulo}
                  onChange={(e) => {
                    setTitulo(e.target.value);
                    if (erros.titulo) setErros((p) => ({ ...p, titulo: undefined }));
                    limparFeedback();
                  }}
                  disabled={enviando}
                  {...fieldProps}
                />
              )}
            </Field>

            {/* Resumo */}
            <Field
              label="Resumo"
              hint="Até 300 caracteres. Aparece nos cartões e na busca."
              id="pub-resumo"
              error={erros.resumo}
            >
              {(fieldProps) => (
                <textarea
                  rows={4}
                  maxLength={MAX_RESUMO}
                  value={resumo}
                  onChange={(e) => {
                    setResumo(e.target.value);
                    if (erros.resumo) setErros((p) => ({ ...p, resumo: undefined }));
                    limparFeedback();
                  }}
                  disabled={enviando}
                  style={{ resize: "vertical", minHeight: 112 }}
                  {...fieldProps}
                />
              )}
            </Field>

            {/* Única parte que muda entre "Escrever" e "Enviar PDF" */}
            <Field
              label="Texto"
              id="pub-texto"
              error={modo === "texto" ? erros.texto : erros.arquivo}
            >
              {(fieldProps) =>
                modo === "texto" ? (
                  <div className="dta-publicar__editor">
                    <EditorToolbar
                      label="Formatação do texto"
                      commands={COMANDOS_EDITOR}
                      onCommand={executarComandoEditor}
                    />
                    <input
                      ref={inputImagemRef}
                      type="file"
                      accept="image/png,image/jpeg,image/webp"
                      onChange={handleSelecionarImagem}
                      style={{ display: "none" }}
                      tabIndex={-1}
                      aria-hidden="true"
                    />
                    {enviandoImagem && (
                      <small>Enviando imagem...</small>
                    )}
                    <textarea
                      ref={textareaRef}
                      rows={10}
                      placeholder="Comece a escrever..."
                      value={texto}
                      onChange={(e) => {
                        setTexto(e.target.value);
                        if (erros.texto) setErros((p) => ({ ...p, texto: undefined }));
                        limparFeedback();
                      }}
                      onKeyDown={handleEditorKeyDown}
                      disabled={enviando}
                      {...fieldProps}
                    />
                  </div>
                ) : (
                  <div
                    className="dta-publicar__pdf-box"
                    data-dragging={arrastandoPdf}
                    onDragOver={(e) => {
                      e.preventDefault();
                      setArrastandoPdf(true);
                    }}
                    onDragLeave={() => setArrastandoPdf(false)}
                    onDrop={(e) => {
                      e.preventDefault();
                      setArrastandoPdf(false);
                      const file = e.dataTransfer.files?.[0];
                      if (file) handleSelecionarPdf(file);
                    }}
                  >
                    <input
                      ref={inputPdfRef}
                      id={fieldProps.id}
                      type="file"
                      accept="application/pdf,.pdf"
                      onChange={(e) => handleSelecionarPdf(e.target.files?.[0])}
                      aria-describedby={fieldProps["aria-describedby"]}
                      aria-invalid={fieldProps["aria-invalid"]}
                      style={{ display: "none" }}
                    />
                    {arquivoPdf ? (
                      <>
                        <div className="dta-publicar__pdf-info">
                          <strong>{arquivoPdf.name}</strong>
                          <span className="dta-meta">
                            PDF selecionado · {formatarTamanho(arquivoPdf.size)}
                          </span>
                        </div>
                        <div style={{ display: "flex", gap: 10, flexWrap: "wrap", justifyContent: "center" }}>
                          <Button onClick={() => inputPdfRef.current?.click()} disabled={enviando}>
                            Trocar PDF
                          </Button>
                          <Button
                            onClick={() => {
                              setArquivoPdf(null);
                              if (inputPdfRef.current) inputPdfRef.current.value = "";
                            }}
                            disabled={enviando}
                          >
                            Remover
                          </Button>
                        </div>
                      </>
                    ) : (
                      <>
                        <div className="dta-publicar__pdf-info">
                          <strong>Envie o arquivo PDF do seu trabalho</strong>
                          <span className="dta-meta">
                            Arraste e solte o PDF aqui ou clique no botão abaixo (máx. 20 MB).
                          </span>
                        </div>
                        <Button onClick={() => inputPdfRef.current?.click()} disabled={enviando}>
                          Selecionar arquivo PDF
                        </Button>
                      </>
                    )}
                  </div>
                )
              }
            </Field>

            {/* Tags e Área do conhecimento lado a lado */}
            <div className="dta-publicar__row">
              <Field label="Tags" id="pub-tags" error={erros.tags}>
                {(fieldProps) => (
                  <>
                    <input
                      type="text"
                      placeholder="ex.: otimização, energia"
                      list="pub-tags-sugestoes"
                      value={tagsInput}
                      onChange={(e) => {
                        setTagsInput(e.target.value);
                        limparFeedback();
                      }}
                      disabled={enviando}
                      {...fieldProps}
                    />
                    {tagsCadastradas.length > 0 && (
                      <datalist id="pub-tags-sugestoes">
                        {tagsCadastradas.map((t) => (
                          <option key={t.id || t.nome} value={t.nome} />
                        ))}
                      </datalist>
                    )}
                  </>
                )}
              </Field>

              <Field label="Área do conhecimento" id="pub-area" error={erros.area}>
                {(fieldProps) => (
                  <select
                    value={area}
                    onChange={(e) => {
                      setArea(e.target.value);
                      limparFeedback();
                    }}
                    disabled={enviando}
                    {...fieldProps}
                  >
                    <option value="">Selecione</option>
                    {opcoesArea.map((opcao) => (
                      <option key={opcao} value={opcao}>
                        {opcao}
                      </option>
                    ))}
                  </select>
                )}
              </Field>
            </div>
          </PublishForm>

          {/* Barra lateral: MEUS TRABALHOS */}
          <aside className="dta-publicar__sidebar" aria-label="Meus trabalhos">
            <span className="dta-kicker">MEUS TRABALHOS</span>

            {meusTrabalhos.length > 0 ? (
              <>
                {emAndamento.map((t) => (
                  <WorkStatusCard
                    key={t.id}
                    titulo={t.titulo}
                    subtitle={`Enviado em ${formatarData(t.data)}`}
                    steps={[
                      { label: "Enviado", done: true },
                      {
                        label: "Em revisão",
                        done: t.status === "revisao" || t.status === "publicado",
                      },
                      {
                        label: "Publicado",
                        done: t.status === "publicado",
                        note: t.status !== "publicado" ? "(pendente)" : undefined,
                      },
                    ]}
                  />
                ))}

                {devolvidos.map((t) => (
                  <ReturnedNotice
                    key={t.id}
                    kicker="DEVOLVIDO COM AJUSTES"
                    titulo={t.titulo}
                    description="O revisor deixou um relatório com pontos a corrigir."
                    actionLabel="Ver relatório e corrigir"
                    onAction={() => iniciarCorrecao(t)}
                  />
                ))}
              </>
            ) : (
              <>
                <WorkStatusCard
                  titulo="[Título do trabalho]"
                  subtitle="Enviado em [data]"
                  steps={[
                    { label: "Enviado", done: true },
                    { label: "Em revisão", done: true },
                    { label: "Publicado", done: false, note: "(pendente)" },
                  ]}
                />

                <ReturnedNotice
                  kicker="DEVOLVIDO COM AJUSTES"
                  titulo="[Título do trabalho]"
                  description="O revisor deixou um relatório com pontos a corrigir."
                  actionLabel="Ver relatório e corrigir"
                  onAction={() => {
                    setTrabalhoEmCorrecao({
                      id: null,
                      titulo: "[Título do trabalho]",
                      resumo: "",
                      tipo: modo,
                      tags: [],
                      revisoes: [
                        {
                          parecer:
                            "Detalhe melhor a metodologia empregada e revise as referências bibliográficas da seção 2.",
                        },
                      ],
                    });
                    if (!titulo) setTitulo("[Título do trabalho]");
                    window.scrollTo({ top: 0, behavior: "smooth" });
                  }}
                />
              </>
            )}
          </aside>
        </div>
      </main>

      <SiteFooter />
    </div>
  );
}
