import { useCallback, useEffect, useState } from "react";
import { useNav } from "../../nav/NavContext.jsx";
import { useAuth } from "../../auth/AuthContext.jsx";
import { SiteHeader } from "../../components/base/SiteHeader.jsx";
import { SiteFooter } from "../../components/base/SiteFooter.jsx";
import { Button } from "../../components/base/Button.jsx";
import { Chip } from "../../components/base/Chip.jsx";
import { Field } from "../../components/base/Field.jsx";
import { EmptyState } from "../../components/home/EmptyState.jsx";
import { DangerConfirmModal } from "../../components/modal/DangerConfirmModal.jsx";
import {
  adminAlterarPerfil,
  adminAnonimizarUsuario,
  adminDespublicar,
  adminListarUsuarios,
  criarTag,
  excluirTag,
  fetchConteudos,
  fetchInstituicoes,
  fetchTags,
  renomearTag,
} from "../../services/api.js";
import { ROTULO_PERFIL, ROTULO_TIPO, formatarData } from "../../services/conteudo.js";
import "../../styles/painel.css";

const ABAS = [
  { id: "usuarios", label: "Usuários" },
  { id: "tags", label: "Tags" },
  { id: "conteudos", label: "Conteúdos publicados" },
];

// O backend recebe o nome do enum (MEMBRO/REVISOR/ADMIN) e devolve a descrição (membro/revisor/admin).
const PERFIS = ["membro", "revisor", "admin"];

function Aviso({ aviso }) {
  if (!aviso?.texto) return null;
  return (
    <p className={`dta-painel__aviso${aviso.erro ? " dta-painel__aviso--erro" : ""}`} role={aviso.erro ? "alert" : "status"}>
      {aviso.texto}
    </p>
  );
}

/** Administração: usuários e perfis, tags e despublicação (/admin/*, /tags). */
export function AdminPage() {
  const [aba, setAba] = useState("usuarios");
  return (
    <div className="dta dta-painel">
      <SiteHeader />
      <main className="dta-wrap">
        <div className="dta-painel__topo">
          <div>
            <span className="dta-kicker">Administração</span>
            <h1 className="dta-serif dta-painel__titulo">Painel administrativo</h1>
          </div>
        </div>
        <div className="dta-painel__abas" role="group" aria-label="Seções">
          {ABAS.map((a) => <Chip key={a.id} pressed={aba === a.id} onClick={() => setAba(a.id)}>{a.label}</Chip>)}
        </div>
        {aba === "usuarios" && <AbaUsuarios />}
        {aba === "tags" && <AbaTags />}
        {aba === "conteudos" && <AbaConteudos />}
      </main>
      <SiteFooter />
    </div>
  );
}

/* ---------------- usuários ---------------- */

function AbaUsuarios() {
  const { usuario: eu } = useAuth();
  const [filtro, setFiltro] = useState({ perfil: "", instituicaoId: "" });
  const [instituicoes, setInstituicoes] = useState([]);
  const [estado, setEstado] = useState({ chave: null, itens: [], erro: "" });
  const [tentativa, setTentativa] = useState(0);
  const [aviso, setAviso] = useState(null);
  const [salvandoId, setSalvandoId] = useState(null);
  const [anonimizando, setAnonimizando] = useState(null);
  const [ocupado, setOcupado] = useState(false);

  const chave = `${filtro.perfil}|${filtro.instituicaoId}|${tentativa}`;
  const carregando = estado.chave !== chave;

  useEffect(() => {
    fetchInstituicoes().then(setInstituicoes).catch(() => setInstituicoes([]));
  }, []);

  useEffect(() => {
    let ativo = true;
    // O backend aplica um filtro por vez (perfil tem prioridade).
    adminListarUsuarios({ perfil: filtro.perfil ? filtro.perfil.toUpperCase() : undefined, instituicaoId: filtro.perfil ? undefined : filtro.instituicaoId || undefined })
      .then((itens) => ativo && setEstado({ chave, itens, erro: "" }))
      .catch((e) => ativo && setEstado({ chave, itens: [], erro: e.message }));
    return () => {
      ativo = false;
    };
  }, [filtro, chave]);

  async function mudarPerfil(u, perfil) {
    setSalvandoId(u.id);
    setAviso(null);
    try {
      await adminAlterarPerfil(u.id, perfil.toUpperCase());
      setEstado((atual) => ({ ...atual, itens: atual.itens.map((x) => (x.id === u.id ? { ...x, perfil } : x)) }));
      setAviso({ texto: `${u.nome} agora é ${ROTULO_PERFIL[perfil].toLowerCase()}.`, erro: false });
    } catch (e) {
      setAviso({ texto: e.message, erro: true });
    } finally {
      setSalvandoId(null);
    }
  }

  async function confirmarAnonimizar() {
    setOcupado(true);
    try {
      await adminAnonimizarUsuario(anonimizando.id);
      setAviso({ texto: `Os dados de ${anonimizando.nome} foram anonimizados.`, erro: false });
      setTentativa((n) => n + 1);
    } catch (e) {
      setAviso({ texto: e.message, erro: true });
    } finally {
      setOcupado(false);
      setAnonimizando(null);
    }
  }

  return (
    <section aria-label="Usuários">
      <Aviso aviso={aviso} />
      <div className="dta-painel__filtros">
        <Field label="Perfil" id="filtro-perfil">
          {(props) => (
            <select value={filtro.perfil} onChange={(e) => setFiltro({ perfil: e.target.value, instituicaoId: "" })} {...props}>
              <option value="">Todos</option>
              {PERFIS.map((p) => <option key={p} value={p}>{ROTULO_PERFIL[p]}</option>)}
            </select>
          )}
        </Field>
        <Field label="Instituição" id="filtro-instituicao">
          {(props) => (
            <select value={filtro.instituicaoId} onChange={(e) => setFiltro({ perfil: "", instituicaoId: e.target.value })} {...props}>
              <option value="">Todas</option>
              {instituicoes.map((i) => <option key={i.id} value={i.id}>{i.sigla || i.nome}</option>)}
            </select>
          )}
        </Field>
      </div>

      {carregando ? (
        <p className="dta-meta" role="status">Carregando usuários…</p>
      ) : estado.erro ? (
        <EmptyState title="Não foi possível carregar os usuários" text={estado.erro} action={<Button onClick={() => setTentativa((n) => n + 1)}>Tentar novamente</Button>} />
      ) : estado.itens.length === 0 ? (
        <EmptyState title="Nenhum usuário encontrado" text="Ajuste os filtros para ver outros usuários." />
      ) : (
        <div className="dta-painel__tabela-wrap">
          <table className="dta-painel__tabela">
            <thead>
              <tr><th scope="col">Nome</th><th scope="col">E-mail</th><th scope="col">Instituição</th><th scope="col">Perfil</th><th scope="col" style={{ textAlign: "right" }}>Ações</th></tr>
            </thead>
            <tbody>
              {estado.itens.map((u) => {
                const souEu = u.id === eu?.id;
                return (
                  <tr key={u.id}>
                    <td>{u.nome}{souEu && <span className="dta-meta"> (você)</span>}</td>
                    <td>{u.email}</td>
                    <td>{u.instituicao?.sigla ?? "—"}</td>
                    <td>
                      <select aria-label={`Perfil de ${u.nome}`} value={u.perfil} disabled={souEu || salvandoId === u.id}
                              onChange={(e) => mudarPerfil(u, e.target.value)}>
                        {PERFIS.map((p) => <option key={p} value={p}>{ROTULO_PERFIL[p]}</option>)}
                      </select>
                    </td>
                    <td style={{ textAlign: "right" }}>
                      <Button variant="danger" disabled={souEu} onClick={() => setAnonimizando(u)}>Anonimizar</Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <DangerConfirmModal
        open={Boolean(anonimizando)}
        busy={ocupado}
        onCancel={() => setAnonimizando(null)}
        onConfirm={confirmarAnonimizar}
        title="Anonimizar usuário?"
        message={anonimizando ? `Nome, e-mail e acesso de ${anonimizando.nome} serão removidos. Os trabalhos permanecem, sem identificação. Esta ação não pode ser desfeita.` : ""}
        confirmLabel="Anonimizar"
      />
    </section>
  );
}

/* ---------------- tags ---------------- */

function AbaTags() {
  const [tags, setTags] = useState(null);
  const [nova, setNova] = useState("");
  const [editando, setEditando] = useState(null); // { id, nome }
  const [excluindo, setExcluindo] = useState(null);
  const [ocupado, setOcupado] = useState(false);
  const [aviso, setAviso] = useState(null);

  const carregar = useCallback(() => fetchTags().then(setTags), []);
  useEffect(() => {
    carregar();
  }, [carregar]);

  const validar = (nome) => {
    if (!nome.trim()) return "Informe o nome da tag.";
    if (nome.includes(",")) return "O nome não pode conter vírgula.";
    if (nome.length > 255) return "Máximo de 255 caracteres.";
    return null;
  };

  async function adicionar(e) {
    e.preventDefault();
    const erro = validar(nova);
    if (erro) return setAviso({ texto: erro, erro: true });
    setOcupado(true);
    try {
      await criarTag(nova.trim());
      setNova("");
      setAviso({ texto: "Tag criada.", erro: false });
      await carregar();
    } catch (err) {
      setAviso({ texto: err.message, erro: true });
    } finally {
      setOcupado(false);
    }
  }

  async function salvarEdicao(e) {
    e.preventDefault();
    const erro = validar(editando.nome);
    if (erro) return setAviso({ texto: erro, erro: true });
    setOcupado(true);
    try {
      await renomearTag(editando.id, editando.nome.trim());
      setEditando(null);
      setAviso({ texto: "Tag renomeada.", erro: false });
      await carregar();
    } catch (err) {
      setAviso({ texto: err.message, erro: true });
    } finally {
      setOcupado(false);
    }
  }

  async function confirmarExclusao() {
    setOcupado(true);
    try {
      await excluirTag(excluindo.id);
      setAviso({ texto: `Tag “${excluindo.nome}” excluída.`, erro: false });
      await carregar();
    } catch (err) {
      setAviso({ texto: err.message, erro: true });
    } finally {
      setOcupado(false);
      setExcluindo(null);
    }
  }

  return (
    <section aria-label="Tags">
      <Aviso aviso={aviso} />
      <form className="dta-painel__form-linha" onSubmit={adicionar} style={{ marginBottom: 24 }}>
        <Field label="Nova tag" id="nova-tag">
          {(props) => <input type="text" value={nova} onChange={(e) => setNova(e.target.value)} {...props} />}
        </Field>
        <button type="submit" className="dta-btn dta-btn--primary" disabled={ocupado}>Adicionar</button>
      </form>

      {tags === null ? (
        <p className="dta-meta" role="status">Carregando tags…</p>
      ) : tags.length === 0 ? (
        <EmptyState title="Nenhuma tag cadastrada" text="Crie tags para que os autores classifiquem os trabalhos." />
      ) : (
        <ul className="dta-painel__lista" style={{ listStyle: "none", margin: 0, padding: 0 }}>
          {tags.map((t) => (
            <li key={t.id} className="dta-painel__item">
              {editando?.id === t.id ? (
                <form className="dta-painel__form-linha" onSubmit={salvarEdicao} style={{ flex: 1 }}>
                  <Field label={`Renomear “${t.nome}”`} id={`tag-${t.id}`}>
                    {(props) => <input type="text" autoFocus value={editando.nome} onChange={(e) => setEditando({ ...editando, nome: e.target.value })} {...props} />}
                  </Field>
                  <button type="submit" className="dta-btn dta-btn--primary" disabled={ocupado}>Salvar</button>
                  <Button onClick={() => setEditando(null)} disabled={ocupado}>Cancelar</Button>
                </form>
              ) : (
                <>
                  <span style={{ fontSize: 16 }}>{t.nome}</span>
                  <div className="dta-painel__item-acoes">
                    <Button onClick={() => setEditando({ id: t.id, nome: t.nome })}>Renomear</Button>
                    <Button variant="danger" onClick={() => setExcluindo(t)}>Excluir</Button>
                  </div>
                </>
              )}
            </li>
          ))}
        </ul>
      )}

      <DangerConfirmModal
        open={Boolean(excluindo)}
        busy={ocupado}
        onCancel={() => setExcluindo(null)}
        onConfirm={confirmarExclusao}
        title="Excluir tag?"
        message={excluindo ? `A tag “${excluindo.nome}” será removida de todos os trabalhos.` : ""}
        confirmLabel="Excluir"
      />
    </section>
  );
}

/* ---------------- conteúdos ---------------- */

const TAMANHO_PAGINA = 20;

function AbaConteudos() {
  const { Link } = useNav();
  const [busca, setBusca] = useState("");
  const [q, setQ] = useState("");
  const [estado, setEstado] = useState({ chave: null, itens: [], pagina: 0, totalPaginas: 0, erro: "" });
  const [tentativa, setTentativa] = useState(0);
  const [alvo, setAlvo] = useState(null);
  const [motivo, setMotivo] = useState("");
  const [erroMotivo, setErroMotivo] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const [aviso, setAviso] = useState(null);

  const chave = `${q}|${tentativa}`;
  const carregando = estado.chave !== chave;

  useEffect(() => {
    let ativo = true;
    fetchConteudos({ q, page: 0, size: TAMANHO_PAGINA })
      .then((res) => ativo && setEstado({ chave, itens: res.content, pagina: 0, totalPaginas: res.page.totalPages, erro: "" }))
      .catch((e) => ativo && setEstado({ chave, itens: [], pagina: 0, totalPaginas: 0, erro: e.message }));
    return () => {
      ativo = false;
    };
  }, [q, chave]);

  async function carregarMais() {
    try {
      const res = await fetchConteudos({ q, page: estado.pagina + 1, size: TAMANHO_PAGINA });
      setEstado((atual) => ({ ...atual, itens: [...atual.itens, ...res.content], pagina: atual.pagina + 1, totalPaginas: res.page.totalPages }));
    } catch (e) {
      setAviso({ texto: e.message, erro: true });
    }
  }

  async function confirmar() {
    if (!motivo.trim()) {
      setErroMotivo("Informe o motivo. Ele será enviado ao autor como parecer.");
      return;
    }
    setOcupado(true);
    try {
      await adminDespublicar(alvo.id, motivo.trim());
      setAviso({ texto: `“${alvo.titulo}” foi despublicado e devolvido ao autor.`, erro: false });
      setEstado((atual) => ({ ...atual, itens: atual.itens.filter((c) => c.id !== alvo.id) }));
      setAlvo(null);
    } catch (e) {
      setErroMotivo(e.message);
    } finally {
      setOcupado(false);
    }
  }

  return (
    <section aria-label="Conteúdos publicados">
      <Aviso aviso={aviso} />
      <form className="dta-painel__form-linha" style={{ marginBottom: 24 }} onSubmit={(e) => { e.preventDefault(); setQ(busca.trim()); }}>
        <Field label="Buscar" id="admin-busca">
          {(props) => <input type="search" value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Título, resumo ou autor" {...props} />}
        </Field>
        <button type="submit" className="dta-btn">Buscar</button>
      </form>

      {carregando ? (
        <p className="dta-meta" role="status">Carregando conteúdos…</p>
      ) : estado.erro ? (
        <EmptyState title="Não foi possível carregar os conteúdos" text={estado.erro} action={<Button onClick={() => setTentativa((n) => n + 1)}>Tentar novamente</Button>} />
      ) : estado.itens.length === 0 ? (
        <EmptyState title="Nenhum conteúdo publicado encontrado" />
      ) : (
        <>
          <ul className="dta-painel__lista" style={{ listStyle: "none", margin: 0, padding: 0 }}>
            {estado.itens.map((c) => (
              <li key={c.id} className="dta-painel__item">
                <div className="dta-painel__item-info">
                  <Link href={`/artigo/${c.id}`} className="dta-serif dta-painel__item-titulo">{c.titulo}</Link>
                  <span className="dta-meta">
                    {[ROTULO_TIPO[c.tipo] ?? c.tipo, (c.autores || []).map((a) => a.nome).join(", "), formatarData(c.data)].filter(Boolean).join(" · ")}
                  </span>
                </div>
                <div className="dta-painel__item-acoes">
                  <Button variant="danger" onClick={() => { setAlvo(c); setMotivo(""); setErroMotivo(""); }}>Despublicar</Button>
                </div>
              </li>
            ))}
          </ul>
          {estado.pagina + 1 < estado.totalPaginas && (
            <div style={{ marginTop: 24, textAlign: "center" }}><Button onClick={carregarMais}>Carregar mais</Button></div>
          )}
        </>
      )}

      <DangerConfirmModal
        open={Boolean(alvo)}
        busy={ocupado}
        onCancel={() => setAlvo(null)}
        onConfirm={confirmar}
        title="Despublicar conteúdo?"
        message={
          <>
            “{alvo?.titulo}” deixará de ser visível ao público e voltará ao autor.
            <Field label="Motivo" id="admin-motivo" error={erroMotivo}>
              {({ id, ...props }) => <textarea id={id} rows={3} value={motivo} onChange={(e) => { setMotivo(e.target.value); setErroMotivo(""); }} {...props} />}
            </Field>
          </>
        }
        confirmLabel="Despublicar"
      />
    </section>
  );
}
