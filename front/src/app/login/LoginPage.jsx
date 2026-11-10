import { useState } from "react";
import { tokens } from "../../tokens.js";
import { useNav } from "../../nav/NavContext.jsx";
import { AuthLayout } from "../../components/base/AuthLayout.jsx";
import { Field } from "../../components/base/Field.jsx";
import { LoginForm } from "../../components/login/LoginForm.jsx";
import { loginUsuario } from "../../services/api.js";

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
    <AuthLayout
      kicker="LOGIN"
      title="Entrar na conta"
      notice={success && <><strong>Login efetuado com sucesso!</strong> Redirecionando...</>}
    >
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
    </AuthLayout>
  );
}
