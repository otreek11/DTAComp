import { useEffect, useMemo, useRef, useState } from "react";
import { SiteHeader } from "../../components/base/SiteHeader.jsx";
import { Field } from "../../components/base/Field.jsx";
import { EmptyState } from "../../components/home/EmptyState.jsx";
import { ArticleBody } from "../../components/article/ArticleBody.jsx";
import { ReviewQueue } from "../../components/review/ReviewQueue.jsx";
import { ReviewPanel } from "../../components/review/ReviewPanel.jsx";
import { AiSuggestionCard } from "../../components/review/AiSuggestionCard.jsx";
import { ConfirmModal } from "../../components/modal/ConfirmModal.jsx";
import { TRABALHOS_MOCK } from "./mockRevisao.js";

import "./revisao.css";

const NAV = [{ label: "Fila de revisão", href: "/revisao", current: true }];

const ABAS = [
  { id: "pendentes", label: "Pendentes" },
  { id: "concluidas", label: "Concluídas" },
];

const ROTULO_TIPO = { texto: "Texto", pdf: "PDF" };
const ROTULO_STATUS_SUGESTAO = { aceita: "Aceita no relatório", ignorada: "Ignorada" };
const RESULTADO = { aprovado: "Aprovado e publicado", devolvido: "Devolvido ao autor" };

function metaDaFila(t, resultado) {
  const tipo = ROTULO_TIPO[t.tipo] ?? t.tipo;
  if (resultado) return `${tipo} · ${RESULTADO[resultado].toLowerCase()} em ${t.data}`;
  return `${tipo} · ${t.reenviado ? "reenviado" : "enviado"} em ${t.data}`;
}

const plural = (n, um, varios) => (n === 1 ? um : varios);

/**
 * Página de revisão: fila · trabalho com trechos apontados pela IA · painel do revisor.
 * Por enquanto trabalha com dados locais (mockRevisao.js); nada é enviado a API.
 */
export function RevisaoPage({ user = { nome: "Revisor" } }) {
  const [aba, setAba] = useState("pendentes");
  const [selecionadoId, setSelecionadoId] = useState(null);
  // resultado por trabalho: "aprovado" | "devolvido" (ausente = pendente)
  const [resultados, setResultados] = useState(() =>
    Object.fromEntries(TRABALHOS_MOCK.filter((t) => t.statusInicial).map((t) => [t.id, t.statusInicial])),
  );
  // rascunho por trabalho: decisões sobre as sugestões e o parecer digitado
  const [rascunhos, setRascunhos] = useState(() =>
    Object.fromEntries(TRABALHOS_MOCK.map((t) => [t.id, { decisoes: t.decisoesIniciais ?? {}, parecer: t.parecerInicial ?? "" }])),
  );
  const [ativo, setAtivo] = useState(null); // { trabalhoId, sugestaoId }
  const [erroParecer, setErroParecer] = useState("");
  const [confirmando, setConfirmando] = useState(false);
  const [aviso, setAviso] = useState("");
  const textoRef = useRef(null);

  const fila = useMemo(
    () => TRABALHOS_MOCK.filter((t) => Boolean(resultados[t.id]) === (aba === "concluidas")),
    [aba, resultados],
  );
  const trabalho = fila.find((t) => t.id === selecionadoId) ?? fila[0] ?? null;
  const resultado = trabalho ? resultados[trabalho.id] : undefined;
  const { decisoes, parecer } = trabalho ? rascunhos[trabalho.id] : { decisoes: {}, parecer: "" };
  const sugestaoAtivaId = trabalho && ativo?.trabalhoId === trabalho.id ? ativo.sugestaoId : null;
  const pendentes = trabalho ? trabalho.sugestoes.filter((s) => !decisoes[s.id]).length : 0;

  // Ao ativar uma sugestão, leva o trecho correspondente à vista.
  useEffect(() => {
    if (!sugestaoAtivaId) return;
    const el = textoRef.current?.querySelector(`[data-sugestao="${sugestaoAtivaId}"]`);
    const reduzMovimento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    el?.scrollIntoView({ block: "nearest", behavior: reduzMovimento ? "auto" : "smooth" });
  }, [sugestaoAtivaId]);

  function atualizarRascunho(parcial) {
    setRascunhos((atual) => ({ ...atual, [trabalho.id]: { ...atual[trabalho.id], ...parcial } }));
  }

  function selecionarTrabalho(id) {
    setSelecionadoId(id);
    setErroParecer("");
    setAviso("");
  }

  function mudarAba(id) {
    setAba(id);
    setSelecionadoId(null);
    setErroParecer("");
  }

  function ativarSugestao(sugestaoId) {
    setAtivo({ trabalhoId: trabalho.id, sugestaoId });
  }

  /** Clicar de novo na mesma decisão volta a sugestão para "sem decisão". */
  function decidir(sugestaoId, valor) {
    const nova = { ...decisoes };
    if (nova[sugestaoId] === valor) delete nova[sugestaoId];
    else nova[sugestaoId] = valor;
    atualizarRascunho({ decisoes: nova });
    if (erroParecer) setErroParecer("");
  }

  function concluir(valor, mensagem) {
    setResultados((atual) => ({ ...atual, [trabalho.id]: valor }));
    setAviso(mensagem);
    setErroParecer("");
    setConfirmando(false);
  }

  function aprovar() {
    // TODO(API): publicar o trabalho no backend antes de atualizar o estado local.
    concluir("aprovado", `“${trabalho.titulo}” foi aprovado e publicado.`);
  }

  function devolver() {
    const aceitas = Object.values(decisoes).filter((d) => d === "aceita").length;
    if (!parecer.trim() && aceitas === 0) {
      setErroParecer("Descreva os erros encontrados ou aceite ao menos uma sugestão da IA.");
      return;
    }
    // TODO(API): enviar parecer + sugestões aceitas ao autor.
    concluir("devolvido", `“${trabalho.titulo}” foi devolvido ao autor com o relatório.`);
  }

  /** Divide o texto do bloco em trechos simples e <mark> (ignoradas deixam de ser destacadas). */
  function renderComMarcas(bloco) {
    const marcas = (bloco.marks ?? [])
      .filter((m) => decisoes[m.sugestaoId] !== "ignorada")
      .sort((a, b) => a.inicio - b.inicio);
    if (marcas.length === 0) return bloco.text;

    const partes = [];
    let cursor = 0;
    marcas.forEach((m) => {
      if (m.inicio < cursor) return; // sobreposição: mantém a primeira
      if (m.inicio > cursor) partes.push(bloco.text.slice(cursor, m.inicio));
      const sugestao = trabalho.sugestoes.find((s) => s.id === m.sugestaoId);
      const trecho = bloco.text.slice(m.inicio, m.fim);
      partes.push(
        <mark
          key={m.sugestaoId}
          data-sugestao={m.sugestaoId}
          data-ativo={m.sugestaoId === sugestaoAtivaId}
          role="button"
          tabIndex={0}
          aria-label={`Sugestão da IA (${sugestao?.categoria ?? "revisão"}): ${trecho}`}
          onClick={() => ativarSugestao(m.sugestaoId)}
          onKeyDown={(e) => {
            if (e.key === "Enter" || e.key === " ") {
              e.preventDefault();
              ativarSugestao(m.sugestaoId);
            }
          }}
        >
          {trecho}
        </mark>,
      );
      cursor = m.fim;
    });
    partes.push(bloco.text.slice(cursor));
    return partes;
  }

  const queue = fila.map((t) => ({ id: t.id, titulo: t.titulo, meta: metaDaFila(t, resultados[t.id]), href: `#trabalho-${t.id}` }));

  const actions = resultado
    ? []
    : [
        { label: "Aprovar e publicar", primary: true, onClick: () => setConfirmando(true) },
        { label: "Devolver ao autor com relatório", onClick: devolver },
      ];
  
  const header = resultado
    ? { kicker: "Trabalho concluído", description: `${RESULTADO[resultado]}. As decisões abaixo estão disponíveis só para leitura.` }
    : { kicker: "Relatório inicial da IA", description: "Sugestões automáticas. A decisão final é sempre do revisor." };
    
  return (
    <div className="dta dta-revisao">
      <SiteHeader user={user} nav={NAV} />

      <main className="dta-wrap">
        {aviso && <p className="dta-revisao__aviso" role="status">{aviso}</p>}

        <div className="dta-revisao__grid">
          <ReviewQueue
            label="Fila de revisão"
            title="Fila de revisão"
            tabs={ABAS}
            tab={aba}
            onTab={mudarAba}
            queue={queue}
            selectedId={trabalho?.id}
            onSelect={selecionarTrabalho}
            empty={
              <EmptyState
                title={aba === "pendentes" ? "Nenhum trabalho pendente" : "Nenhum trabalho concluído"}
                text={aba === "pendentes" ? "Novos envios aparecerão aqui." : "Os trabalhos que você aprovar ou devolver aparecerão aqui."}
              />
            }
          />

          <section className="dta-revisao__texto" aria-label="Trabalho em revisão" ref={textoRef}>
            {trabalho ? (
              <>
                <span className="dta-kicker">{ROTULO_TIPO[trabalho.tipo]} · {trabalho.data}</span>
                <h2 className="dta-serif dta-revisao__titulo">{trabalho.titulo}</h2>
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
                  <ArticleBody blocks={trabalho.blocks} renderText={renderComMarcas} />
                )}
              </>
            ) : (
              <EmptyState title="Nenhum trabalho selecionado" text="Escolha um trabalho na fila para começar a revisão." />
            )}
          </section>

          {trabalho && (
            <ReviewPanel label="Painel do revisor" header={header} actions={actions}>
              {trabalho.sugestoes.length === 0 && <p className="dta-meta" style={{ margin: 0 }}>A IA não encontrou sugestões para este trabalho.</p>}
              {trabalho.sugestoes.map((s) => (
                <AiSuggestionCard
                  key={s.id}
                  sugestao={s}
                  acceptLabel="Aceitar"
                  ignoreLabel="Ignorar"
                  status={decisoes[s.id]}
                  statusLabel={ROTULO_STATUS_SUGESTAO[decisoes[s.id]]}
                  active={s.id === sugestaoAtivaId}
                  readOnly={Boolean(resultado)}
                  onSelect={() => ativarSugestao(s.id)}
                  onAccept={() => decidir(s.id, "aceita")}
                  onIgnore={() => decidir(s.id, "ignorada")}
                />
              ))}
              <Field label="Parecer do revisor" id="parecer" error={erroParecer}>
                {({ id, ...props }) => (
                  <textarea
                    id={id}
                    rows={6}
                    placeholder="Descreva os erros encontrados"
                    value={parecer}
                    readOnly={Boolean(resultado)}
                    onChange={(e) => {
                      atualizarRascunho({ parecer: e.target.value });
                      if (erroParecer) setErroParecer("");
                    }}
                    {...props}
                  />
                )}
              </Field>
            </ReviewPanel>
          )}
        </div>
      </main>

      {trabalho && (
        <ConfirmModal
          open={confirmando}
          onCancel={() => setConfirmando(false)}
          onConfirm={aprovar}
          title="Aprovar e publicar?"
          message={
            pendentes > 0
              ? `Ainda ${plural(pendentes, "há 1 sugestão", `há ${pendentes} sugestões`)} da IA sem decisão. Deseja aprovar e publicar “${trabalho.titulo}” mesmo assim?`
              : `“${trabalho.titulo}” ficará visível para todos os leitores.`
          }
          confirmLabel="Aprovar e publicar"
        />
      )}
    </div>
  );
}
