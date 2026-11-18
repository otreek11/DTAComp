import { useEffect, useState } from "react";
import { useNav } from "../../nav/NavContext.jsx";
import { useAuth } from "../../auth/AuthContext.jsx";
import { SiteHeader } from "../../components/base/SiteHeader.jsx";
import { SiteFooter } from "../../components/base/SiteFooter.jsx";
import { Button } from "../../components/base/Button.jsx";
import { Field } from "../../components/base/Field.jsx";
import { DangerConfirmModal } from "../../components/modal/DangerConfirmModal.jsx";
import { alterarSenha, atualizarPerfil, excluirMinhaConta, exportarMeusDados, fetchInstituicoes } from "../../services/api.js";
import { ROTULO_PERFIL } from "../../services/conteudo.js";
import "../../styles/painel.css";

function Mensagem({ estado }) {
  if (!estado?.texto) return null;
  return (
    <p className={`dta-painel__aviso${estado.erro ? " dta-painel__aviso--erro" : ""}`} role={estado.erro ? "alert" : "status"} style={{ margin: 0 }}>
      {estado.texto}
    </p>
  );
}

/** Minha conta: perfil, senha, exportação de dados (LGPD) e exclusão (/usuarios/me/*). */
export function ContaPage() {
  const { usuario, atualizarUsuario, sair } = useAuth();
  const { navigate } = useNav();

  return (
    <div className="dta dta-painel">
      <SiteHeader />
      <main className="dta-wrap">
        <div className="dta-painel__topo">
          <div>
            <span className="dta-kicker">{ROTULO_PERFIL[usuario?.perfil] ?? "Conta"}</span>
            <h1 className="dta-serif dta-painel__titulo">Minha conta</h1>
            <p className="dta-meta" style={{ margin: "8px 0 0" }}>{usuario?.email}</p>
          </div>
        </div>

        <div className="dta-painel__secoes">
          <SecaoPerfil usuario={usuario} onAtualizado={atualizarUsuario} />
          <SecaoSenha />
          <SecaoExportar />
          <SecaoExcluir onExcluida={() => { sair(); navigate("/"); }} />
        </div>
      </main>
      <SiteFooter />
    </div>
  );
}

function SecaoPerfil({ usuario, onAtualizado }) {
  const [instituicoes, setInstituicoes] = useState([]);
  const [nome, setNome] = useState(usuario?.nome ?? "");
  const [idInstituicao, setIdInstituicao] = useState(usuario?.instituicao?.id ?? "");
  const [erros, setErros] = useState({});
  const [estado, setEstado] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    let ativo = true;
    fetchInstituicoes()
      .then((lista) => ativo && setInstituicoes(lista))
      .catch((e) => ativo && setEstado({ texto: e.message, erro: true }));
    return () => {
      ativo = false;
    };
  }, []);

  async function salvar(e) {
    e.preventDefault();
    const novos = {};
    if (!nome.trim()) novos.nome = "Informe seu nome.";
    else if (nome.trim().length > 255) novos.nome = "O nome deve ter no máximo 255 caracteres.";
    if (!idInstituicao) novos.idInstituicao = "Selecione sua instituição.";
    setErros(novos);
    if (Object.keys(novos).length) return;

    setEnviando(true);
    setEstado(null);
    try {
      const atualizado = await atualizarPerfil({ nome: nome.trim(), idInstituicao });
      onAtualizado({ nome: atualizado.nome, instituicao: atualizado.instituicao });
      setEstado({ texto: "Perfil atualizado.", erro: false });
    } catch (err) {
      setEstado({ texto: err.message, erro: true });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="dta-painel__secao" onSubmit={salvar} noValidate>
      <h2 className="dta-serif">Perfil</h2>
      <Field label="Nome completo" id="conta-nome" error={erros.nome}>
        {(props) => <input type="text" autoComplete="name" value={nome} onChange={(e) => setNome(e.target.value)} {...props} />}
      </Field>
      <Field label="Instituição" id="conta-instituicao" error={erros.idInstituicao}>
        {(props) => (
          <select value={idInstituicao} onChange={(e) => setIdInstituicao(e.target.value)} {...props}>
            <option value="">Selecione…</option>
            {instituicoes.map((i) => (
              <option key={i.id} value={i.id}>{i.sigla ? `${i.sigla} · ${i.nome}` : i.nome}</option>
            ))}
          </select>
        )}
      </Field>
      <Mensagem estado={estado} />
      <div><button type="submit" className="dta-btn dta-btn--primary" disabled={enviando}>{enviando ? "Salvando…" : "Salvar perfil"}</button></div>
    </form>
  );
}

function SecaoSenha() {
  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmacao, setConfirmacao] = useState("");
  const [erros, setErros] = useState({});
  const [estado, setEstado] = useState(null);
  const [enviando, setEnviando] = useState(false);

  async function salvar(e) {
    e.preventDefault();
    const novos = {};
    if (!senhaAtual) novos.senhaAtual = "Informe a senha atual.";
    if (novaSenha.length < 8 || novaSenha.length > 72) novos.novaSenha = "A nova senha deve ter entre 8 e 72 caracteres.";
    if (confirmacao !== novaSenha) novos.confirmacao = "As senhas não coincidem.";
    setErros(novos);
    if (Object.keys(novos).length) return;

    setEnviando(true);
    setEstado(null);
    try {
      await alterarSenha({ senhaAtual, novaSenha });
      setSenhaAtual("");
      setNovaSenha("");
      setConfirmacao("");
      setEstado({ texto: "Senha alterada.", erro: false });
    } catch (err) {
      setEstado({ texto: err.message, erro: true });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="dta-painel__secao" onSubmit={salvar} noValidate>
      <h2 className="dta-serif">Senha</h2>
      <Field label="Senha atual" id="conta-senha-atual" error={erros.senhaAtual}>
        {(props) => <input type="password" autoComplete="current-password" value={senhaAtual} onChange={(e) => setSenhaAtual(e.target.value)} {...props} />}
      </Field>
      <Field label="Nova senha" id="conta-nova-senha" hint="Entre 8 e 72 caracteres." error={erros.novaSenha}>
        {(props) => <input type="password" autoComplete="new-password" value={novaSenha} onChange={(e) => setNovaSenha(e.target.value)} {...props} />}
      </Field>
      <Field label="Confirme a nova senha" id="conta-confirmacao" error={erros.confirmacao}>
        {(props) => <input type="password" autoComplete="new-password" value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} {...props} />}
      </Field>
      <Mensagem estado={estado} />
      <div><button type="submit" className="dta-btn dta-btn--primary" disabled={enviando}>{enviando ? "Salvando…" : "Alterar senha"}</button></div>
    </form>
  );
}

function SecaoExportar() {
  const [estado, setEstado] = useState(null);
  const [enviando, setEnviando] = useState(false);

  async function exportar() {
    setEnviando(true);
    setEstado(null);
    try {
      const dados = await exportarMeusDados();
      const blob = new Blob([JSON.stringify(dados, null, 2)], { type: "application/json" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `dtacomp-meus-dados-${new Date().toISOString().slice(0, 10)}.json`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      setTimeout(() => URL.revokeObjectURL(url), 1000);
      setEstado({ texto: "Arquivo gerado.", erro: false });
    } catch (err) {
      setEstado({ texto: err.message, erro: true });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section className="dta-painel__secao">
      <h2 className="dta-serif">Meus dados</h2>
      <p>Baixe uma cópia dos dados da sua conta, dos trabalhos enviados e das revisões feitas, em formato JSON (LGPD).</p>
      <Mensagem estado={estado} />
      <div><Button onClick={exportar} disabled={enviando}>{enviando ? "Gerando…" : "Exportar meus dados"}</Button></div>
    </section>
  );
}

function SecaoExcluir({ onExcluida }) {
  const [aberto, setAberto] = useState(false);
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function confirmar() {
    if (!senha) {
      setErro("Informe sua senha para confirmar.");
      return;
    }
    setEnviando(true);
    try {
      await excluirMinhaConta(senha);
      setAberto(false);
      onExcluida();
    } catch (err) {
      setErro(err.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section className="dta-painel__secao dta-painel__secao--perigo">
      <h2 className="dta-serif">Excluir conta</h2>
      <p>Seus dados pessoais serão removidos. Esta ação não pode ser desfeita.</p>
      <div><Button variant="danger" onClick={() => { setSenha(""); setErro(""); setAberto(true); }}>Excluir minha conta</Button></div>
      <DangerConfirmModal
        open={aberto}
        busy={enviando}
        onCancel={() => setAberto(false)}
        onConfirm={confirmar}
        title="Excluir sua conta?"
        message={
          <>
            Digite sua senha para confirmar a exclusão definitiva da conta.
            <Field label="Senha" id="excluir-senha" error={erro}>
              {(props) => (
                <input type="password" autoComplete="current-password" value={senha} onChange={(e) => { setSenha(e.target.value); setErro(""); }} {...props} />
              )}
            </Field>
          </>
        }
        confirmLabel="Excluir definitivamente"
      />
    </section>
  );
}
