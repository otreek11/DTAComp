import { useState } from "react";
import { tokens } from "../../tokens.js";
import { useNav } from "../../nav/NavContext.jsx";
import { Field } from "../base/Field.jsx";
import { LoginForm } from "./LoginForm.jsx";
import { loginUsuario } from "../../services/api.js";
import "../signup/SignUpPage.css";

/**
 * Tela de Login do DTA · UFMA.
 * Segue o mesmo esquema visual split-screen do cadastro, utilizando os componentes base
 * e os tokens de design do projeto.
 */
export function LoginPage({ onNavigateToSignUp, onSuccess }) {
  const { Link } = useNav();

  const [form, setForm] = useState({
    email: "",
    senha: "",
  });

  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

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

    if (!form.email.trim()) {
      newErrors.email = "E-mail é obrigatório.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      newErrors.email = "Informe um e-mail válido.";
    }

    if (!form.senha) {
      newErrors.senha = "Senha é obrigatória.";
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
      const response = await loginUsuario({
        email: form.email.trim(),
        senha: form.senha,
      });

      if (response?.token) {
        localStorage.setItem("dtacomp_token", response.token);
        localStorage.setItem("dtacomp_user", JSON.stringify(response));
      }

      setSuccess(true);
      if (onSuccess) {
        setTimeout(() => onSuccess(response), 1000);
      }
    } catch (err) {
      setServerError(err.message || "E-mail ou senha inválidos.");
    } finally {
      setSubmitting(false);
    }
  }

  const footer = (
    <span style={{ color: tokens.inkSoft, fontSize: 14 }}>
      Não tem conta?{" "}
      <Link
        href="/cadastro"
        onClick={(e) => {
          if (onNavigateToSignUp) {
            e.preventDefault();
            onNavigateToSignUp();
          }
        }}
        style={{ fontWeight: 600, color: tokens.ink, textDecoration: "none" }}
      >
        Criar conta
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

      {/* Coluna Direita: Formulário de Login */}
      <main className="dta-auth-form-panel">
        <div style={{ width: "100%", maxWidth: 440 }}>
          <div className="dta-kicker" style={{ marginBottom: 8 }}>
            LOGIN
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
            Entrar na conta
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
              <strong>Login efetuado com sucesso!</strong> Redirecionando...
            </div>
          )}

          <LoginForm
            onSubmit={handleSubmit}
            serverError={serverError}
            submitLabel={submitting ? "Entrando..." : "Entrar"}
            disabled={submitting}
            footer={footer}
          >
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
            <Field label="Senha" id="senha" error={errors.senha}>
              {({ id, ...props }) => (
                <input
                  id={id}
                  type="password"
                  placeholder="••••••••"
                  value={form.senha}
                  onChange={(e) => handleFieldChange("senha", e.target.value)}
                  disabled={submitting}
                  autoComplete="current-password"
                  {...props}
                />
              )}
            </Field>
          </LoginForm>
        </div>
      </main>
    </div>
  );
}
