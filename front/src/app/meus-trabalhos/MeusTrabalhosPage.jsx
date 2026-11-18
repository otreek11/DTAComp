import { useCallback, useEffect, useState } from "react";
import { useNav } from "../../nav/NavContext.jsx";
import { SiteHeader } from "../../components/base/SiteHeader.jsx";
import { SiteFooter } from "../../components/base/SiteFooter.jsx";
import { Button } from "../../components/base/Button.jsx";
import { Chip } from "../../components/base/Chip.jsx";
import { EmptyState } from "../../components/home/EmptyState.jsx";
import { DangerConfirmModal } from "../../components/modal/DangerConfirmModal.jsx";
import { excluirConteudo, fetchMeusConteudos } from "../../services/api.js";
import { ROTULO_STATUS, ROTULO_TIPO, formatarData } from "../../services/conteudo.js";
import "../../styles/painel.css";

const TAMANHO_PAGINA = 20;

const ABAS = [
  { id: "", label: "Todos" },
  { id: "revisao", label: "Em revisão" },
  { id: "devolvido", label: "Devolvidos" },
  { id: "publicado", label: "Publicados" },
];

const VAZIO = {
  "": { title: "Você ainda não enviou trabalhos", text: "Escreva um artigo ou envie um PDF para revisão." },
  revisao: { title: "Nada em revisão", text: "Trabalhos enviados aguardam aqui a decisão de um revisor." },
  devolvido: { title: "Nenhum trabalho devolvido", text: "Quando um revisor pedir ajustes, o trabalho aparecerá aqui." },
  publicado: { title: "Nenhum trabalho publicado", text: "Trabalhos aprovados ficam visíveis a todos em Explorar." },
};

/** Lista de trabalhos do usuário autenticado (GET /usuarios/me/conteudos). */
export function MeusTrabalhosPage() {
  const { Link, navigate } = useNav();
  const [status, setStatus] = useState("");
  const [lista, setLista] = useState({ status: null, itens: [], pagina: 0, totalPaginas: 0, erro: "" });
  const [tentativa, setTentativa] = useState(0);
  const [carregandoMais, setCarregandoMais] = useState(false);
  const [excluindo, setExcluindo] = useState(null);
  const [ocupado, setOcupado] = useState(false);
  const [aviso, setAviso] = useState({ texto: "", erro: false });

  const chave = `${status}|${tentativa}`;
  const carregando = lista.status !== chave;

  useEffect(() => {
    let ativo = true;
    fetchMeusConteudos({ status, page: 0, size: TAMANHO_PAGINA })
      .then((res) => ativo && setLista({ status: chave, itens: res.content, pagina: 0, totalPaginas: res.page.totalPages, erro: "" }))
      .catch((e) => ativo && setLista({ status: chave, itens: [], pagina: 0, totalPaginas: 0, erro: e.message }));
    return () => {
      ativo = false;
    };
  }, [status, chave]);

  const carregarMais = useCallback(async () => {
    setCarregandoMais(true);
    try {
      const res = await fetchMeusConteudos({ status, page: lista.pagina + 1, size: TAMANHO_PAGINA });
      setLista((atual) => ({ ...atual, itens: [...atual.itens, ...res.content], pagina: atual.pagina + 1, totalPaginas: res.page.totalPages }));
    } catch (e) {
      setAviso({ texto: e.message, erro: true });
    } finally {
      setCarregandoMais(false);
    }
  }, [status, lista.pagina]);

  async function confirmarExclusao() {
    setOcupado(true);
    try {
      await excluirConteudo(excluindo.id);
      setAviso({ texto: `“${excluindo.titulo}” foi excluído.`, erro: false });
      setLista((atual) => ({ ...atual, itens: atual.itens.filter((t) => t.id !== excluindo.id) }));
      setExcluindo(null);
    } catch (e) {
      setAviso({ texto: e.message, erro: true });
      setExcluindo(null);
    } finally {
      setOcupado(false);
    }
  }

  const vazio = VAZIO[status];

  return (
    <div className="dta dta-painel">
      <SiteHeader />
      <main className="dta-wrap">
        <div className="dta-painel__topo">
          <div>
            <span className="dta-kicker">Painel do membro</span>
            <h1 className="dta-serif dta-painel__titulo">Meus trabalhos</h1>
          </div>
          <Link href="/publicar" className="dta-btn dta-btn--primary" style={{ display: "inline-flex", alignItems: "center", textDecoration: "none", color: "#fff" }}>
            Publicar novo trabalho
          </Link>
        </div>

        {aviso.texto && (
          <p className={`dta-painel__aviso${aviso.erro ? " dta-painel__aviso--erro" : ""}`} role={aviso.erro ? "alert" : "status"}>{aviso.texto}</p>
        )}

        <div className="dta-painel__abas" role="group" aria-label="Filtrar por situação">
          {ABAS.map((a) => (
            <Chip key={a.id || "todos"} pressed={status === a.id} onClick={() => { setStatus(a.id); setAviso({ texto: "", erro: false }); }}>
              {a.label}
            </Chip>
          ))}
        </div>

        {carregando ? (
          <p className="dta-meta" role="status">Carregando seus trabalhos…</p>
        ) : lista.erro ? (
          <EmptyState title="Não foi possível carregar seus trabalhos" text={lista.erro} action={<Button onClick={() => setTentativa((n) => n + 1)}>Tentar novamente</Button>} />
        ) : lista.itens.length === 0 ? (
          <EmptyState title={vazio.title} text={vazio.text} />
        ) : (
          <>
            <ul className="dta-painel__lista" style={{ listStyle: "none", margin: 0, padding: 0 }}>
              {lista.itens.map((t) => (
                <li key={t.id} className="dta-painel__item">
                  <div className="dta-painel__item-info">
                    <span>
                      <span className={`dta-painel__status dta-painel__status--${t.status}`}>{ROTULO_STATUS[t.status] ?? t.status}</span>
                    </span>
                    <Link href={`/artigo/${t.id}`} className="dta-serif dta-painel__item-titulo">{t.titulo}</Link>
                    <span className="dta-meta">
                      {[ROTULO_TIPO[t.tipo] ?? t.tipo, formatarData(t.data) && `enviado em ${formatarData(t.data)}`, (t.tags || []).join(", ")].filter(Boolean).join(" · ")}
                    </span>
                  </div>
                  <div className="dta-painel__item-acoes">
                    {t.status === "devolvido" && (
                      <Button variant="primary" onClick={() => navigate(`/publicar?editar=${encodeURIComponent(t.id)}`)}>
                        Ver parecer e corrigir
                      </Button>
                    )}
                    <Link href={`/artigo/${t.id}`} className="dta-btn" style={{ display: "inline-flex", alignItems: "center", textDecoration: "none" }}>
                      Ver
                    </Link>
                    <Button variant="danger" onClick={() => setExcluindo(t)}>Excluir</Button>
                  </div>
                </li>
              ))}
            </ul>
            {lista.pagina + 1 < lista.totalPaginas && (
              <div style={{ marginTop: 24, textAlign: "center" }}>
                <Button onClick={carregarMais} disabled={carregandoMais}>{carregandoMais ? "Carregando…" : "Carregar mais"}</Button>
              </div>
            )}
          </>
        )}
      </main>
      <SiteFooter />

      <DangerConfirmModal
        open={Boolean(excluindo)}
        busy={ocupado}
        onCancel={() => setExcluindo(null)}
        onConfirm={confirmarExclusao}
        title="Excluir trabalho?"
        message={excluindo ? `“${excluindo.titulo}” será removido definitivamente, incluindo arquivos e pareceres. Esta ação não pode ser desfeita.` : ""}
        confirmLabel="Excluir"
      />
    </div>
  );
}
