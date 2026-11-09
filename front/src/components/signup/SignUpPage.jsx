import { useState, useEffect } from "react";
import { tokens } from "../../tokens.js";
import { useNav } from "../../nav/NavContext.jsx";
import { Field } from "../base/Field.jsx";
import { CheckboxField } from "../base/CheckboxField.jsx";
import { SignUpForm } from "./SignUpForm.jsx";
import { fetchInstituicoes, registerUsuario } from "../../services/api.js";
import "./SignUpPage.css";

/**
 * Tela de Cadastro de Usuário do DTA · UFMA.
 * Implementada utilizando os componentes base da biblioteca (Field, CheckboxField, SignUpForm)
 * e os tokens de design do projeto.
 */
export function SignUpPage({ onNavigateToLogin, onSuccess }) {
  const { Link } = useNav();

  const [instituicoes, setInstituicoes] = useState([]);
  const [loadingInstituicoes, setLoadingInstituicoes] = useState(true);

  const [form, setForm] = useState({
    nome: "",
    email: "",
    senha: "",
    idInstituicao: "",
    lgpd: false,
  });

  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let isMounted = true;
    async function loadInstituicoes() {
      try {
        const list = await fetchInstituicoes();
        if (isMounted) {
          setInstituicoes(list);
          // Pré-seleciona a UFMA se encontrada, conforme o modelo visual
          const ufma = list.find(
            (i) => i.sigla?.toUpperCase() === "UFMA" || i.nome?.toLowerCase().includes("maranhão")
          );
          if (ufma) {
            setForm((prev) => ({ ...prev, idInstituicao: ufma.id }));
          } else if (list.length > 0) {
            setForm((prev) => ({ ...prev, idInstituicao: list[0].id }));
          }
        }
      } finally {
        if (isMounted) {
          setLoadingInstituicoes(false);
        }
      }
    }
    loadInstituicoes();
    return () => {
      isMounted = false;
    };
  }, []);

  function handleFieldChange(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
    if (serverError) {
      setServerError("");
    }
  }

  function validate() {
    const newErrors = {};

    if (!form.nome.trim()) {
      newErrors.nome = "Nome é obrigatório.";
    }

    if (!form.email.trim()) {
      newErrors.email = "E-mail é obrigatório.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      newErrors.email = "Informe um e-mail válido.";
    }

    if (!form.senha) {
      newErrors.senha = "Senha é obrigatória.";
    } else if (form.senha.length < 8) {
      newErrors.senha = "A senha deve ter no mínimo 8 caracteres.";
    } else if (!/(?=.*[a-zA-Z])(?=.*[0-9])/.test(form.senha)) {
      newErrors.senha = "A senha deve conter letras e números.";
    }

    if (!form.idInstituicao) {
      newErrors.idInstituicao = "Selecione uma instituição.";
    }

    if (!form.lgpd) {
      newErrors.lgpd = "É necessário aceitar os termos da LGPD para continuar.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setServerError("");

    if (!validate()) {
      return;
    }

    setSubmitting(true);

    try {
      const response = await registerUsuario({
        nome: form.nome.trim(),
        email: form.email.trim(),
        senha: form.senha,
        idInstituicao: form.idInstituicao,
      });

      if (response?.token) {
        localStorage.setItem("dtacomp_token", response.token);
        localStorage.setItem("dtacomp_user", JSON.stringify(response));
      }

      setSuccess(true);
      if (onSuccess) {
        setTimeout(onSuccess, 1500);
      }
    } catch (err) {
      setServerError(err.message || "Não foi possível criar sua conta. Tente novamente.");
    } finally {
      setSubmitting(false);
    }
  }

  const footer = (
    <span style={{ color: tokens.inkSoft, fontSize: 14 }}>
      Já tem conta?{" "}
      <Link
        href="/login"
        onClick={(e) => {
          if (onNavigateToLogin) {
            e.preventDefault();
            onNavigateToLogin();
          }
        }}
        style={{ fontWeight: 600, color: tokens.ink, textDecoration: "none" }}
      >
        Entrar
      </Link>
    </span>
  );

  return (
    <div className="dta dta-auth-layout">
      {/* Coluna Esquerda: Marca e Mensagem Editorial */}
      <section className="dta-auth-hero" aria-label="Apresentação institucional">
        <div
          className="dta-serif"
          style={{ fontSize: 26, fontWeight: 700, letterSpacing: "-0.02em", color: "#ffffff" }}
        >
          DTA·UFMA
        </div>

        <div className="dta-auth-hero-body">
          <h1
            className="dta-serif"
            style={{
              fontSize: "clamp(30px, 3.6vw, 46px)",
              fontWeight: 700,
              lineHeight: 1.15,
              letterSpacing: "-0.025em",
              color: "#ffffff",
              margin: "0 0 24px",
            }}
          >
            Publique o que você pesquisa. Leia o que a comunidade descobre.
          </h1>
          <p
            style={{
              fontSize: 17,
              lineHeight: 1.55,
              color: "rgba(255, 255, 255, 0.82)",
              margin: 0,
            }}
          >
            Com uma conta de membro, você envia textos e PDFs, recebe a revisão e divulga seu trabalho.
          </p>
        </div>

        <div style={{ fontSize: 13.5, color: "rgba(255, 255, 255, 0.65)" }}>
          Ler continua livre, sem precisar de conta.
        </div>
      </section>

      {/* Coluna Direita: Formulário de Cadastro */}
      <main className="dta-auth-form-panel">
        <div style={{ width: "100%", maxWidth: 440 }}>
          <div className="dta-kicker" style={{ marginBottom: 8 }}>
            CADASTRO
          </div>
          <h2
            className="dta-serif"
            style={{
              fontSize: 34,
              fontWeight: 700,
              margin: "0 0 28px",
              color: tokens.ink,
              lineHeight: 1.2,
            }}
          >
            Criar conta
          </h2>

          {success && (
            <div
              role="status"
              style={{
                backgroundColor: "#f0fdf4",
                border: "1px solid #bbf7d0",
                color: "#166534",
                padding: "12px 16px",
                borderRadius: 4,
                marginBottom: 20,
                fontSize: 14,
                lineHeight: 1.45,
              }}
            >
              <strong>Conta criada com sucesso!</strong> Seja bem-vindo(a) ao DTA · UFMA.
            </div>
          )}

          <SignUpForm
            onSubmit={handleSubmit}
            serverError={serverError}
            submitLabel={submitting ? "Criando conta..." : "Criar conta"}
            disabled={submitting}
            footer={footer}
          >
            {/* Nome Completo */}
            <Field label="Nome completo" id="nome" error={errors.nome}>
              {({ id, ...props }) => (
                <input
                  id={id}
                  type="text"
                  placeholder="Seu nome"
                  value={form.nome}
                  onChange={(e) => handleFieldChange("nome", e.target.value)}
                  disabled={submitting}
                  autoComplete="name"
                  {...props}
                />
              )}
            </Field>

            {/* E-mail */}
            <Field label="E-mail" id="email" error={errors.email}>
              {({ id, ...props }) => (
                <input
                  id={id}
                  type="email"
                  placeholder="voce@exemplo.com"
                  value={form.email}
                  onChange={(e) => handleFieldChange("email", e.target.value)}
                  disabled={submitting}
                  autoComplete="email"
                  {...props}
                />
              )}
            </Field>

            {/* Senha */}
            <Field
              label="Senha"
              hint="Mínimo de 8 caracteres, com letras e números."
              id="senha"
              error={errors.senha}
            >
              {({ id, ...props }) => (
                <input
                  id={id}
                  type="password"
                  placeholder="••••••••"
                  value={form.senha}
                  onChange={(e) => handleFieldChange("senha", e.target.value)}
                  disabled={submitting}
                  autoComplete="new-password"
                  {...props}
                />
              )}
            </Field>

            {/* Instituição */}
            <Field label="Instituição" id="instituicao" error={errors.idInstituicao}>
              {({ id, ...props }) => (
                <select
                  id={id}
                  value={form.idInstituicao}
                  onChange={(e) => handleFieldChange("idInstituicao", e.target.value)}
                  disabled={submitting || loadingInstituicoes}
                  {...props}
                >
                  {loadingInstituicoes ? (
                    <option value="">Carregando instituições...</option>
                  ) : instituicoes.length === 0 ? (
                    <option value="">Nenhuma instituição disponível</option>
                  ) : (
                    instituicoes.map((inst) => (
                      <option key={inst.id} value={inst.id}>
                        {inst.sigla ? `${inst.sigla} — ${inst.nome}` : inst.nome}
                      </option>
                    ))
                  )}
                </select>
              )}
            </Field>

            {/* Aceite LGPD */}
            <CheckboxField
              id="lgpd"
              label="Li e aceito o tratamento dos meus dados conforme a política de privacidade (LGPD)."
              checked={form.lgpd}
              onChange={(e) => handleFieldChange("lgpd", e.target.checked)}
              error={errors.lgpd}
            />
          </SignUpForm>
        </div>
      </main>
    </div>
  );
}
