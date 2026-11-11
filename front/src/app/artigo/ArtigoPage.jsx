import { useEffect, useMemo, useRef, useState } from "react";
import { useNav } from "../../nav/NavContext.jsx";
import { OpenReadingBanner } from "../../components/base/OpenReadingBanner.jsx";
import { SiteHeader } from "../../components/base/SiteHeader.jsx";
import { SiteFooter } from "../../components/base/SiteFooter.jsx";
import { Cover } from "../../components/base/Cover.jsx";
import { EmptyState } from "../../components/home/EmptyState.jsx";
import { ArticleBody } from "../../components/article/ArticleBody.jsx";
import { TableOfContents } from "../../components/article/TableOfContents.jsx";
import { ReaderTools } from "../../components/article/ReaderTools.jsx";
import { AboutWork } from "../../components/article/AboutWork.jsx";
import { Breadcrumb } from "../../components/article/Breadcrumb.jsx";
import { ArticleByline } from "../../components/article/ArticleByline.jsx";
import { RelatedList } from "../../components/article/RelatedList.jsx";
import { ConfirmModal } from "../../components/modal/ConfirmModal.jsx";
import { ImageViewerModal } from "../../components/modal/ImageViewerModal.jsx";
import { ARTIGOS_MOCK } from "./mockArtigo.js";
import "./artigo.css";

const ROTULO_TIPO = { texto: "Artigo", pdf: "PDF" };
const MESES_ABNT = ["jan.", "fev.", "mar.", "abr.", "maio", "jun.", "jul.", "ago.", "set.", "out.", "nov.", "dez."];
const MESES_CURTOS = ["jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"];

const CHAVE_LEITURA = "dtacomp_leitura";
const FONTE_MIN = -4;
const FONTE_MAX = 10;
const limitarFonte = (n) => Math.min(FONTE_MAX, Math.max(FONTE_MIN, n));

/** Preferências de leitura (tamanho da fonte e contraste) lembradas entre artigos. */
function lerPreferencias() {
  try {
    const v = JSON.parse(localStorage.getItem(CHAVE_LEITURA));
    return { delta: Number.isFinite(v?.delta) ? limitarFonte(v.delta) : 0, contraste: Boolean(v?.contraste) };
  } catch {
    return { delta: 0, contraste: false };
  }
}

function salvarPreferencias(prefs) {
  try {
    localStorage.setItem(CHAVE_LEITURA, JSON.stringify(prefs));
  } catch {
    /* armazenamento indisponível: a preferência vale só nesta visita */
  }
}

/** "2026-11-02" -> [dia, mês(0-11), ano] sem fuso (a data do backend é LocalDate). */
function partesData(iso) {
  const [ano, mes, dia] = (iso || "").split("-").map(Number);
  return ano && mes && dia ? [dia, mes - 1, ano] : null;
}

function formatarData(iso) {
  const p = partesData(iso);
  return p ? `${p[0]} ${MESES_CURTOS[p[1]]} ${p[2]}` : null;
}

function formatarDataAbnt(iso) {
  const p = partesData(iso);
  return p ? `${p[0]} ${MESES_ABNT[p[1]]} ${p[2]}` : "";
}

const nomesAutores = (autores = []) => autores.map((a) => a.nome).filter(Boolean);
const siglasInstituicao = (autores = []) => [...new Set(autores.map((a) => a.siglaInstituicao).filter(Boolean))];

/** "Ana Beatriz Lima" -> "LIMA, Ana Beatriz" */
function nomeAbnt(nome) {
  const partes = nome.trim().split(/\s+/);
  if (partes.length < 2) return nome.toUpperCase();
  return `${partes[partes.length - 1].toUpperCase()}, ${partes.slice(0, -1).join(" ")}`;
}

function minutosDeLeitura(blocks = []) {
  const palavras = blocks.map((b) => b.text || "").join(" ").split(/\s+/).filter(Boolean).length;
  return Math.max(1, Math.round(palavras / 200));
}

function paraRelacionado(a) {
  return {
    id: a.id,
    kicker: a.area ?? ROTULO_TIPO[a.tipo],
    titulo: a.titulo,
    meta: [nomesAutores(a.autores).join(", "), formatarData(a.data)].filter(Boolean),
    href: `/artigo/${a.id}`,
  };
}

/**
 * Página de um trabalho publicado. `id` vem da rota (/artigo/:id).
 * Por enquanto usa dados locais (mockArtigo.js); nada é buscado em API.
 */
export function ArtigoPage({ id = null, user = null, onSignIn }) {
  const { Link } = useNav();
  // TODO(API): buscar o conteúdo por id. Sem id (/artigo), cai no primeiro exemplo para o menu funcionar.
  const artigo = id ? ARTIGOS_MOCK.find((a) => a.id === id) : ARTIGOS_MOCK[0];

  if (!artigo) {
    return (
      <div className="dta dta-artigo">
        <OpenReadingBanner />
        <SiteHeader user={user} onSignIn={onSignIn} />
        <main className="dta-wrap dta-artigo__vazio">
          <EmptyState
            title="Trabalho não encontrado"
            text="O endereço pode estar incorreto ou o trabalho não está mais disponível."
            action={<Link href="/" className="dta-btn dta-btn--primary" style={{ display: "inline-flex", alignItems: "center", textDecoration: "none", color: "#fff" }}>Voltar para Explorar</Link>}
          />
        </main>
        <SiteFooter />
      </div>
    );
  }

  // key: ao trocar de trabalho, o estado de leitura (modais, avisos) recomeça limpo
  return <ArtigoLeitura key={artigo.id} artigo={artigo} user={user} onSignIn={onSignIn} />;
}

function ArtigoLeitura({ artigo, user, onSignIn }) {
  const [prefs, setPrefs] = useState(lerPreferencias);
  const [ativoId, setAtivoId] = useState(null);
  const [progresso, setProgresso] = useState(0);
  const [imagem, setImagem] = useState(null);
  const [citando, setCitando] = useState(false);
  const [citacaoCopiada, setCitacaoCopiada] = useState(false);
  const [aviso, setAviso] = useState("");
  const corpoRef = useRef(null);
  const ferramentasRef = useRef(null);

  const headings = useMemo(
    () => artigo.blocks.filter((b) => b.type === "h2" && b.id).map((b) => ({ id: b.id, label: b.text })),
    [artigo],
  );
  const relacionados = ARTIGOS_MOCK.filter((a) => a.id !== artigo.id).slice(0, 3).map(paraRelacionado);
  const autores = nomesAutores(artigo.autores);
  const siglas = siglasInstituicao(artigo.autores);
  const area = artigo.area ?? ROTULO_TIPO[artigo.tipo];
  const departamento = artigo.autores?.find((a) => a.departamento)?.departamento;
  const minutos = artigo.tipo === "texto" ? minutosDeLeitura(artigo.blocks) : null;

  const citacao = useMemo(() => {
    const hoje = new Date();
    const acesso = `${hoje.getDate()} ${MESES_ABNT[hoje.getMonth()]} ${hoje.getFullYear()}`;
    return `${autores.map(nomeAbnt).join("; ")}. ${artigo.titulo}. DTA·UFMA – Divulgação de Trabalhos Acadêmicos, São Luís, ${formatarDataAbnt(artigo.data)}. Disponível em: ${window.location.href}. Acesso em: ${acesso}.`;
  }, [artigo, autores]);

  // Barra de progresso de leitura: quanto do artigo já foi percorrido.
  useEffect(() => {
    let quadro = 0;
    function calcular() {
      quadro = 0;
      const el = corpoRef.current;
      if (!el) return;
      const r = el.getBoundingClientRect();
      const total = r.height - window.innerHeight;
      setProgresso(total <= 0 ? 100 : Math.round(Math.min(100, Math.max(0, (-r.top / total) * 100))));
    }
    function agendar() {
      if (!quadro) quadro = requestAnimationFrame(calcular);
    }
    window.addEventListener("scroll", agendar, { passive: true });
    window.addEventListener("resize", agendar);
    agendar();
    return () => {
      cancelAnimationFrame(quadro);
      window.removeEventListener("scroll", agendar);
      window.removeEventListener("resize", agendar);
    };
  }, []);

  // Seção em foco no índice.
  useEffect(() => {
    if (headings.length === 0) return undefined;
    const alvos = headings.map((h) => document.getElementById(h.id)).filter(Boolean);
    const obs = new IntersectionObserver(
      (entradas) => {
        const visivel = entradas.filter((e) => e.isIntersecting).sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)[0];
        if (visivel) setAtivoId(visivel.target.id);
      },
      { rootMargin: "-10% 0px -75% 0px" },
    );
    alvos.forEach((el) => obs.observe(el));
    return () => obs.disconnect();
  }, [headings]);

  useEffect(() => {
    if (!aviso) return undefined;
    const t = setTimeout(() => setAviso(""), 4000);
    return () => clearTimeout(t);
  }, [aviso]);

  function atualizarPrefs(parcial) {
    const novas = { ...prefs, ...parcial };
    setPrefs(novas);
    salvarPreferencias(novas);
  }

  async function compartilhar() {
    const url = window.location.href;
    try {
      if (navigator.share) {
        await navigator.share({ title: artigo.titulo, url });
        return;
      }
      await navigator.clipboard.writeText(url);
      setAviso("Link copiado.");
    } catch (e) {
      if (e?.name !== "AbortError") setAviso("Não foi possível compartilhar. Copie o endereço na barra do navegador.");
    }
  }

  async function copiarCitacao() {
    try {
      await navigator.clipboard.writeText(citacao);
      setCitacaoCopiada(true);
    } catch {
      setCitacaoCopiada(false);
    }
  }

  function fecharCitacao() {
    setCitando(false);
    setCitacaoCopiada(false);
  }

  function irParaFerramentas() {
    ferramentasRef.current?.scrollIntoView({ block: "center" });
    ferramentasRef.current?.focus();
  }

  const autoria = [autores.join(", "), siglas.join(", ")].filter(Boolean).join(" · ");
  const meta = [artigo.data && `Publicado em ${formatarData(artigo.data)}`, minutos && `${minutos} min de leitura`].filter(Boolean);

  return (
    <div className="dta dta-artigo">
      <OpenReadingBanner />
      <SiteHeader user={user} onSignIn={onSignIn} onAccessibility={irParaFerramentas} progress={progresso} />

      <div
        className={`dta dta-artigo__leitura${prefs.contraste ? " dta-hc" : ""}`}
        style={{ "--dta-font": `calc(var(--dta-font-base) + ${prefs.delta}px)` }}
      >
        <main className="dta-wrap">
          {aviso && <p className="dta-artigo__aviso" role="status">{aviso}</p>}

          <Breadcrumb items={[{ label: "Explorar", href: "/" }, { label: area }]} />

          <header className="dta-artigo__cabecalho">
            <span className="dta-kicker">{area}</span>
            <h1 className="dta-serif">{artigo.titulo}</h1>
            {artigo.resumo && <p className="dta-artigo__resumo dta-serif">{artigo.resumo}</p>}
            <ArticleByline name={autoria} meta={meta} />
          </header>

          <div className="dta-artigo__grid">
            <div className="dta-artigo__lateral">
              <div className="dta-artigo__toc">
                {headings.length > 0 && <TableOfContents title="Neste artigo" headings={headings} activeId={ativoId ?? headings[0].id} />}
              </div>

              <aside ref={ferramentasRef} className="dta-artigo__ferramentas" aria-label="Ferramentas de leitura" tabIndex={-1}>
                <ReaderTools
                  onShare={compartilhar}
                  onCite={() => setCitando(true)}
                  onFontSize={(passo) => atualizarPrefs({ delta: limitarFonte(prefs.delta + passo) })}
                  onContrast={() => atualizarPrefs({ contraste: !prefs.contraste })}
                />
              </aside>
            </div>

            <div className="dta-artigo__coluna">
              {artigo.capa && (
                <figure className="dta-artigo__capa">
                  <Cover src={artigo.capa} alt={artigo.capaAlt} height={380} />
                  {artigo.capaAlt && <figcaption className="dta-meta">{artigo.capaAlt}</figcaption>}
                </figure>
              )}

              <article ref={corpoRef} className="dta-artigo__corpo" aria-label={artigo.titulo}>
                {artigo.tipo === "pdf" ? (
                  <object
                    data={artigo.arquivoURL}
                    type="application/pdf"
                    className="dta-revisao__pdf-viewer"
                    aria-label={`PDF do trabalho: ${artigo.titulo}`}
                  >
                    <p className="dta-meta">
                      Seu navegador não exibe PDFs aqui.{" "}
                      <a href={artigo.arquivoURL} target="_blank" rel="noreferrer">Abrir em nova aba</a>
                    </p>
                  </object>
                ) : (
                  <ArticleBody blocks={artigo.blocks} onImageClick={setImagem} />
                )}
              </article>

              <AboutWork
                label="Sobre o trabalho"
                name={autores.join(", ")}
                subtitle={[siglas.join(", "), departamento].filter(Boolean).join(" · ")}
                rows={[
                  { label: "Instituição", value: siglas.join(", ") },
                  artigo.revisor && { label: "Revisado por", value: artigo.revisor },
                  { label: "Publicado em", value: formatarData(artigo.data) },
                ].filter((r) => r && r.value)}
                tags={artigo.tags}
                onTag={(tag) => window.location.assign(`/?tag=${encodeURIComponent(tag)}`)}
                // TODO(API): trocar pelo fluxo de denúncia quando existir; por ora aponta às diretrizes.
                footer={<>Encontrou algo impróprio? <a href="/diretrizes">Denunciar conteúdo</a></>}
              />
            </div>
          </div>

          <RelatedList title="Leia também" items={relacionados} />
        </main>
      </div>

      <SiteFooter />

      <ImageViewerModal open={Boolean(imagem)} onClose={() => setImagem(null)} src={imagem?.src} alt={imagem?.alt} caption={imagem?.caption} />
      <ConfirmModal
        open={citando}
        onCancel={fecharCitacao}
        onConfirm={copiarCitacao}
        title="Citar este trabalho"
        message={
          <>
            {citacao}
            {citacaoCopiada && <><br /><strong role="status">Citação copiada.</strong></>}
          </>
        }
        cancelLabel="Fechar"
        confirmLabel="Copiar citação"
      />
    </div>
  );
}
