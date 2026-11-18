import { useCallback, useEffect, useMemo, useState } from "react";
import { tokens } from "../../tokens.js";
import { SiteHeader } from "../../components/base/SiteHeader.jsx";
import { Field } from "../../components/base/Field.jsx";
import { Button } from "../../components/base/Button.jsx";
import { EmptyState } from "../../components/home/EmptyState.jsx";
import { ArticleBody } from "../../components/article/ArticleBody.jsx";
import { ReviewQueue } from "../../components/review/ReviewQueue.jsx";
import { ReviewPanel } from "../../components/review/ReviewPanel.jsx";
import { ConfirmModal } from "../../components/modal/ConfirmModal.jsx";
import { fetchConteudoDetalhe, fetchMinhasRevisoes, fetchPendentes, registrarRevisao, urlDaApi } from "../../services/api.js";
import { ROTULO_TIPO, formatarData, htmlParaBlocos } from "../../services/conteudo.js";

import "./revisao.css";

const ABAS = [
  { id: "pendentes", label: "Pendentes" },
  { id: "concluidas", label: "Concluídas" },
];

const RESULTADO = { publicado: "Aprovado e publicado", devolvido: "Devolvido ao autor" };

function semChave(obj, chave) {
  const copia = { ...obj };
  delete copia[chave];
  return copia;
}

const nomesAutores = (autores = []) => autores.map((a) => a.nome).filter(Boolean).join(", ");

/**
 * Página de revisão: fila · trabalho · painel do revisor.
 * Pendentes: GET /revisoes/pendentes. Concluídas: GET /revisoes/minhas.
 * Decisão: POST /revisoes { idConteudo, aprovado, parecer }.
 * (O painel de sugestões da IA fica oculto até existir endpoint no backend.)
 */
export function RevisaoPage() {
  const [aba, setAba] = useState("pendentes");
  const [pendentes, setPendentes] = useState({ itens: [], carregado: false, erro: "" });
  const [concluidas, setConcluidas] = useState({ itens: [], carregado: false, erro: "" });
  const [selecionadoId, setSelecionadoId] = useState(null);
  const [detalhes, setDetalhes] = useState({}); // idConteudo -> { dados } | { erro }
  const [pareceres, setPareceres] = useState({}); // idConteudo -> texto digitado
  const [erroParecer, setErroParecer] = useState("");
  const [confirmando, setConfirmando] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [aviso, setAviso] = useState("");

  const carregarPendentes = useCallback(
    () =>
      fetchPendentes()
        .then((res) => setPendentes({ itens: res.content, carregado: true, erro: "" }))
        .catch((e) => setPendentes({ itens: [], carregado: true, erro: e.message })),
    [],
  );

  const carregarConcluidas = useCallback(
    () =>
      fetchMinhasRevisoes()
        .then((lista) => setConcluidas({ itens: lista, carregado: true, erro: "" }))
        .catch((e) => setConcluidas({ itens: [], carregado: true, erro: e.message })),
    [],
  );

  useEffect(() => {
    let ativo = true;
    fetchPendentes()
      .then((res) => ativo && setPendentes({ itens: res.content, carregado: true, erro: "" }))
      .catch((e) => ativo && setPendentes({ itens: [], carregado: true, erro: e.message }));
    fetchMinhasRevisoes()
      .then((lista) => ativo && setConcluidas({ itens: lista, carregado: true, erro: "" }))
      .catch((e) => ativo && setConcluidas({ itens: [], carregado: true, erro: e.message }));
    return () => {
      ativo = false;
    };
  }, []);

  // Itens da fila normalizados: { id (chave da fila), idConteudo, titulo, meta, resultado?, parecer? }
  const fila = useMemo(() => {
    if (aba === "pendentes") {
      return pendentes.itens.map((c) => ({
        id: c.id,
        idConteudo: c.id,
        titulo: c.titulo,
        meta: [ROTULO_TIPO[c.tipo] ?? c.tipo, `enviado em ${formatarData(c.data)}`, nomesAutores(c.autores)].filter(Boolean).join(" · "),
      }));
    }
    return concluidas.itens.map((r) => ({
      id: r.id,
      idConteudo: r.idConteudo,
      titulo: r.tituloConteudo ?? "Trabalho removido",
      meta: [ROTULO_TIPO[r.tipoConteudo] ?? r.tipoConteudo, `${(RESULTADO[r.status] ?? r.status).toLowerCase()} em ${formatarData(r.data)}`].filter(Boolean).join(" · "),
      resultado: r.status,
      parecer: r.parecer,
    }));
  }, [aba, pendentes.itens, concluidas.itens]);

  const estadoAba = aba === "pendentes" ? pendentes : concluidas;
  const item = fila.find((t) => t.id === selecionadoId) ?? fila[0] ?? null;
  const idConteudo = item?.idConteudo ?? null;
  const detalhe = idConteudo ? detalhes[idConteudo] : undefined;
  const trabalho = detalhe?.dados ?? null;
  const somenteLeitura = aba === "concluidas";
  const parecer = idConteudo ? pareceres[idConteudo] ?? "" : "";

  // Carrega (uma vez) o conteúdo completo do item selecionado.
  useEffect(() => {
    if (!idConteudo || detalhes[idConteudo]) return undefined;
    let ativo = true;
    fetchConteudoDetalhe(idConteudo)
      .then((dados) => {
        if (!ativo) return;
        const blocks = dados.tipo === "texto" ? htmlParaBlocos(dados.texto?.corpoHtml, dados.texto?.imagens) : [];
        setDetalhes((atual) => ({ ...atual, [idConteudo]: { dados: { ...dados, blocks, arquivoURL: urlDaApi(dados.pdf?.url) } } }));
      })
      .catch((e) => ativo && setDetalhes((atual) => ({ ...atual, [idConteudo]: { erro: e.message } })));
    return () => {
      ativo = false;
    };
  }, [idConteudo, detalhes]);

  function selecionar(id) {
    setSelecionadoId(id);
    setErroParecer("");
  }

  function mudarAba(id) {
    setAba(id);
    setSelecionadoId(null);
    setErroParecer("");
  }

  function aoDigitarParecer(texto) {
    setPareceres((atual) => ({ ...atual, [idConteudo]: texto }));
    if (erroParecer) setErroParecer("");
  }

  async function decidir(aprovado) {
    if (!aprovado && !parecer.trim()) {
      setErroParecer("Descreva os ajustes necessários para devolver o trabalho ao autor.");
      return;
    }
    const titulo = item.titulo;
    setEnviando(true);
    try {
      await registrarRevisao({ idConteudo, aprovado, parecer });
      setAviso(aprovado ? `“${titulo}” foi aprovado e publicado.` : `“${titulo}” foi devolvido ao autor com o parecer.`);
      setPendentes((atual) => ({ ...atual, itens: atual.itens.filter((c) => c.id !== idConteudo) }));
      setPareceres((atual) => semChave(atual, idConteudo));
      setDetalhes((atual) => semChave(atual, idConteudo));
      setSelecionadoId(null);
      carregarConcluidas();
    } catch (e) {
      if (e.status === 409) {
        setAviso(`“${titulo}” não está mais em revisão (provavelmente já foi revisado por outra pessoa). A fila foi atualizada.`);
        setSelecionadoId(null);
        carregarPendentes();
        carregarConcluidas();
      } else {
        setErroParecer(e.message);
      }
    } finally {
      setEnviando(false);
      setConfirmando(false);
    }
  }

  const reenviado = !somenteLeitura && (trabalho?.revisoes?.length ?? 0) > 0;
  const pareceresAnteriores = (trabalho?.revisoes ?? []).filter((r) => r.parecer);

  const actions = somenteLeitura || !trabalho
    ? []
    : [
        { label: enviando ? "Enviando…" : "Aprovar e publicar", primary: true, disabled: enviando, onClick: () => setConfirmando(true) },
        { label: "Devolver ao autor com parecer", disabled: enviando, onClick: () => decidir(false) },
      ];

  const header = somenteLeitura
    ? { kicker: "Revisão concluída", description: `${RESULTADO[item?.resultado] ?? "Concluída"}. Disponível só para leitura.` }
    : { kicker: reenviado ? "Trabalho reenviado" : "Novo envio", description: reenviado ? "O autor corrigiu o trabalho após um parecer anterior." : "A decisão de publicar ou devolver é do revisor." };

  return (
    <div className="dta dta-revisao">
      <SiteHeader />

      <main className="dta-wrap">
        {aviso && <p className="dta-revisao__aviso" role="status">{aviso}</p>}

        <div className="dta-revisao__grid">
          <ReviewQueue
            label="Fila de revisão"
            title="Fila de revisão"
            tabs={ABAS}
            tab={aba}
            onTab={mudarAba}
            queue={fila.map((t) => ({ ...t, href: `#trabalho-${t.id}` }))}
            selectedId={item?.id}
            onSelect={selecionar}
            empty={
              !estadoAba.carregado ? (
                <p className="dta-meta" role="status">Carregando…</p>
              ) : estadoAba.erro ? (
                <EmptyState
                  title="Não foi possível carregar a fila"
                  text={estadoAba.erro}
                  action={<Button onClick={aba === "pendentes" ? carregarPendentes : carregarConcluidas}>Tentar novamente</Button>}
                />
              ) : (
                <EmptyState
                  title={aba === "pendentes" ? "Nenhum trabalho pendente" : "Nenhuma revisão concluída"}
                  text={aba === "pendentes" ? "Novos envios aparecerão aqui." : "Os trabalhos que você aprovar ou devolver aparecerão aqui."}
                />
              )
            }
          />

          <section className="dta-revisao__texto" aria-label="Trabalho em revisão" aria-busy={Boolean(item && !detalhe)}>
            {!item ? (
              <EmptyState title="Nenhum trabalho selecionado" text="Escolha um trabalho na fila para começar a revisão." />
            ) : detalhe?.erro ? (
              <EmptyState title="Não foi possível abrir o trabalho" text={detalhe.erro} />
            ) : !trabalho ? (
              <p className="dta-meta" role="status">Carregando trabalho…</p>
            ) : (
              <>
                <span className="dta-kicker">{ROTULO_TIPO[trabalho.tipo]} · {formatarData(trabalho.data)}</span>
                <h2 className="dta-serif dta-revisao__titulo">{trabalho.titulo}</h2>
                {trabalho.autores?.length > 0 && (
                  <p className="dta-meta" style={{ marginTop: -8 }}>
                    {trabalho.autores.map((a) => [a.nome, a.siglaInstituicao].filter(Boolean).join(" · ")).join("; ")}
                  </p>
                )}
                {trabalho.resumo && <p className="dta-serif" style={{ fontStyle: "italic", color: tokens.inkSoft }}>{trabalho.resumo}</p>}
                {trabalho.tags?.length > 0 && <p className="dta-meta">Tags: {trabalho.tags.join(", ")}</p>}
                {trabalho.tipo === "pdf" ? (
                  <object
                    data={trabalho.arquivoURL}
                    type="application/pdf"
                    className="dta-revisao__pdf-viewer"
                    aria-label={`PDF do trabalho: ${trabalho.titulo}`}
                  >
                    <p className="dta-meta">
                      Seu navegador não exibe PDFs aqui.{" "}
                      <a href={trabalho.arquivoURL} target="_blank" rel="noreferrer">Abrir em nova aba</a>
                    </p>
                  </object>
                ) : (
                  <ArticleBody blocks={trabalho.blocks} />
                )}
              </>
            )}
          </section>

          {item && (
            <ReviewPanel label="Painel do revisor" header={header} actions={actions}>
              {pareceresAnteriores.length > 0 && !somenteLeitura && (
                <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                  <span className="dta-kicker">Pareceres anteriores</span>
                  {pareceresAnteriores.map((r) => (
                    <blockquote key={r.id} style={{ margin: 0, padding: "8px 12px", borderLeft: `3px solid ${tokens.line}`, fontSize: 14, color: tokens.inkSoft }}>
                      {r.parecer}
                    </blockquote>
                  ))}
                </div>
              )}
              {somenteLeitura ? (
                <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                  <span className="dta-kicker">Seu parecer</span>
                  <p style={{ margin: 0, fontSize: 15, lineHeight: 1.5 }}>{item.parecer || <span className="dta-meta">Sem parecer escrito.</span>}</p>
                </div>
              ) : (
                <Field label="Parecer do revisor" id="parecer" hint="Obrigatório para devolver; opcional ao aprovar." error={erroParecer}>
                  {({ id, ...props }) => (
                    <textarea
                      id={id}
                      rows={6}
                      placeholder="Descreva os ajustes necessários"
                      value={parecer}
                      disabled={enviando || !trabalho}
                      onChange={(e) => aoDigitarParecer(e.target.value)}
                      {...props}
                    />
                  )}
                </Field>
              )}
            </ReviewPanel>
          )}
        </div>
      </main>

      {item && (
        <ConfirmModal
          open={confirmando}
          busy={enviando}
          onCancel={() => setConfirmando(false)}
          onConfirm={() => decidir(true)}
          title="Aprovar e publicar?"
          message={`“${item.titulo}” ficará visível para todos os leitores.`}
          confirmLabel="Aprovar e publicar"
        />
      )}
    </div>
  );
}
