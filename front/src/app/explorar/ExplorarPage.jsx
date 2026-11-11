import { useCallback, useEffect, useState } from "react";
import { OpenReadingBanner } from "../../components/base/OpenReadingBanner.jsx";
import { SiteHeader } from "../../components/base/SiteHeader.jsx";
import { SiteFooter } from "../../components/base/SiteFooter.jsx";
import { Button } from "../../components/base/Button.jsx";
import { HeroIntro } from "../../components/home/HeroIntro.jsx";
import { SearchBar } from "../../components/home/SearchBar.jsx";
import { FeaturedArticle } from "../../components/home/FeaturedArticle.jsx";
import { RecentList } from "../../components/home/RecentList.jsx";
import { FilterBar } from "../../components/home/FilterBar.jsx";
import { ArticleGrid } from "../../components/home/ArticleGrid.jsx";
import { EmptyState } from "../../components/home/EmptyState.jsx";
import { fetchConteudos, fetchTags } from "../../services/api.js";
import "./explorar.css";

const TAMANHO_PAGINA = 6;

const TIPOS = [
  { value: "", label: "Todos" },
  { value: "texto", label: "Artigos" },
  { value: "pdf", label: "PDFs" },
];

const ORDENACOES = [
  { value: "data,desc", label: "Mais recentes" },
  { value: "data,asc", label: "Mais antigos" },
  { value: "titulo,asc", label: "Título (A–Z)" },
];

const ROTULO_TIPO = { texto: "Artigo", pdf: "PDF" };

const formatadorData = new Intl.DateTimeFormat("pt-BR", { day: "numeric", month: "short", year: "numeric" });

/** "2026-10-12" -> "12 out 2026" (sem fuso: a data do backend é LocalDate). */
function formatarData(iso) {
  if (!iso) return null;
  const [ano, mes, dia] = iso.split("-").map(Number);
  if (!ano || !mes || !dia) return null;
  return formatadorData.format(new Date(ano, mes - 1, dia)).replace(/\./g, "").replace(/ de /g, " ");
}

function nomesAutores(autores = []) {
  const nomes = autores.map((a) => a.nome).filter(Boolean);
  return nomes.length ? nomes.join(", ") : null;
}

function siglaInstituicao(autores = []) {
  return autores.find((a) => a.siglaInstituicao)?.siglaInstituicao ?? null;
}

/** Converte ConteudoResumoResponse no formato usado pelos cards. */
function paraCard(conteudo) {
  return {
    id: conteudo.id,
    titulo: conteudo.titulo,
    resumo: conteudo.resumo,
    kicker: ROTULO_TIPO[conteudo.tipo] ?? conteudo.tipo,
    meta: [nomesAutores(conteudo.autores), siglaInstituicao(conteudo.autores), formatarData(conteudo.data)].filter(Boolean),
    href: `/artigo/${conteudo.id}`,
  };
}

/**
 * Página Explorar (início): destaque + recém-publicados e a listagem filtrável
 * de trabalhos publicados, consumindo GET /conteudos e GET /tags.
 */
export function ExplorarPage({ user = null, onSignIn }) {
  // Destaque e recém-publicados (independentes dos filtros)
  const [destaques, setDestaques] = useState([]);

  // Filtros
  const [busca, setBusca] = useState("");
  const [q, setQ] = useState("");
  const [tipo, setTipo] = useState("");
  const [tag, setTag] = useState(() => new URLSearchParams(window.location.search).get("tag") ?? "");
  const [sort, setSort] = useState("data,desc");
  const [tags, setTags] = useState([]);
  

  // Listagem: `lista.chave` identifica a consulta que gerou o resultado; enquanto
  // difere da consulta atual, a página está carregando.
  const [tentativa, setTentativa] = useState(0);
  const [lista, setLista] = useState({ chave: null, itens: [], pagina: 0, totalPaginas: 0, erro: "" });
  const [carregandoMais, setCarregandoMais] = useState(false);
  const chave = JSON.stringify({ q, tipo, tag, sort, tentativa });
  const carregando = lista.chave !== chave;
  const { itens, pagina, totalPaginas, erro } = lista;

  useEffect(() => {
    let ativo = true;
    fetchConteudos({ size: 4, sort: "data,desc" })
      .then((res) => ativo && setDestaques(res.content.map(paraCard)))
      .catch(() => ativo && setDestaques([]));
    fetchTags().then((lista) => ativo && setTags(lista));
    return () => { ativo = false; };
  }, []);

  useEffect(() => {
    let ativo = true;
    fetchConteudos({ q, tipo, tags: tag ? [tag] : [], sort, page: 0, size: TAMANHO_PAGINA })
      .then((res) => {
        if (!ativo) return;
        setLista({
          chave,
          itens: res.content.map(paraCard),
          pagina: res.page.number ?? 0,
          totalPaginas: res.page.totalPages ?? 0,
          erro: "",
        });
      })
      .catch((e) => {
        if (!ativo) return;
        setLista({ chave, itens: [], pagina: 0, totalPaginas: 0, erro: e.message || "Não foi possível carregar os trabalhos." });
      });
    return () => { ativo = false; };
  }, [chave, q, tipo, tag, sort]);

  const carregarMais = useCallback(async () => {
    setCarregandoMais(true);
    try {
      const res = await fetchConteudos({ q, tipo, tags: tag ? [tag] : [], sort, page: pagina + 1, size: TAMANHO_PAGINA });
      setLista((atual) => {
        if (atual.chave !== chave) return atual;
        const vistos = new Set(atual.itens.map((i) => i.id));
        return {
          ...atual,
          itens: [...atual.itens, ...res.content.map(paraCard).filter((i) => !vistos.has(i.id))],
          pagina: res.page.number ?? atual.pagina + 1,
          totalPaginas: res.page.totalPages ?? atual.totalPaginas,
          erro: "",
        };
      });
    } catch (e) {
      setLista((atual) => ({ ...atual, erro: e.message || "Não foi possível carregar mais trabalhos." }));
    } finally {
      setCarregandoMais(false);
    }
  }, [chave, q, tipo, tag, sort, pagina]);

  function limparFiltros() {
    setBusca("");
    setQ("");
    setTipo("");
    setTag("");
    setSort("data,desc");
  }

  const filtrosAtivos = Boolean(q || tipo || tag);
  const [destaque, ...recentes] = destaques;

  return (
    <div className="dta dta-explorar">
      <OpenReadingBanner />
      <SiteHeader user={user} onSignIn={onSignIn} onAccessibility={() => {}} />

      <main className="dta-wrap">
        <HeroIntro
          title="Ciência em desenvolvimento, ao alcance de toda a comunidade."
          description="Artigos, estudos e blogs de estudantes e pesquisadores da UFMA, revisados antes de publicar."
        >
          <SearchBar value={busca} onChange={setBusca} onSearch={(v) => setQ((v || "").trim())} />
        </HeroIntro>

        {destaque && (
          <section className="dta-explorar__top" aria-label="Destaques">
            <a href={destaque.href} className="dta-explorar__featured">
              <FeaturedArticle item={{ ...destaque, kicker: `Destaque · ${destaque.kicker}` }} />
            </a>
            {recentes.length > 0 && (
              <div className="dta-explorar__recent">
                <RecentList
                  title="Recém-publicados"
                  items={recentes.map((r) => ({ ...r, meta: [r.kicker, r.meta[r.meta.length - 1]].filter(Boolean) }))}
                />
              </div>
            )}
          </section>
        )}

        <section className="dta-explorar__list" aria-labelledby="explorar-titulo">
          <div className="dta-explorar__head">
            <h2 id="explorar-titulo" className="dta-serif">Explorar</h2>
            <label className="dta-explorar__sort">
              <span>Ordenar por</span>
              <select value={sort} onChange={(e) => setSort(e.target.value)}>
                {ORDENACOES.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
            </label>
          </div>

          <div className="dta-explorar__filters">
            <FilterBar label="Tipo de trabalho" options={TIPOS} active={tipo} onChange={setTipo} className="dta-filterbar--scroll" />
            {tags.length > 0 && (
              <FilterBar
                label="Tags"
                options={tags.map((t) => ({ value: t.nome, label: t.nome }))}
                active={tag}
                onChange={(v) => setTag((atual) => (atual === v ? "" : v))}
                className="dta-filterbar--scroll dta-explorar__tags"
              />
            )}
          </div>

          <div aria-live="polite" aria-busy={carregando}>
            {carregando ? (
              <p className="dta-explorar__status">Carregando trabalhos…</p>
            ) : erro && !itens.length ? (
              <EmptyState
                title="Não foi possível carregar os trabalhos"
                text={erro}
                action={<Button onClick={() => setTentativa((n) => n + 1)}>Tentar novamente</Button>}
              />
            ) : (
              <>
                <ArticleGrid
                  className="dta-grid--compact"
                  items={itens}
                  hasMore={pagina + 1 < totalPaginas}
                  loadMoreLabel="Ver mais trabalhos"
                  onLoadMore={carregarMais}
                  loadingMore={carregandoMais}
                  empty={
                    <EmptyState
                      title="Nenhum trabalho encontrado"
                      text={filtrosAtivos ? "Tente outros termos ou remova os filtros." : "Ainda não há trabalhos publicados."}
                      action={filtrosAtivos && <Button onClick={limparFiltros}>Limpar filtros</Button>}
                    />
                  }
                />
                {erro && itens.length > 0 && <p className="dta-explorar__status" role="alert">{erro}</p>}
              </>
            )}
          </div>
        </section>
      </main>

      <SiteFooter />
    </div>
  );
}
