/**
 * Estrutura do formulário de login. Campos (Field…), validação e envio
 * são responsabilidade da página: ela passa os campos em children e trata onSubmit.
 * serverError: node/texto de erro geral. footer: node abaixo do botão.
 */
export function LoginForm({ onSubmit, serverError, submitLabel = "Entrar", footer, disabled = false, children }) {
  return (
    <form onSubmit={onSubmit} noValidate style={{ display: "flex", flexDirection: "column", gap: 20, width: "100%", maxWidth: 440 }}>
      {serverError && <div role="alert" style={{ color: "var(--dta-warn)", fontSize: 14 }}>{serverError}</div>}
      {children}
      <button type="submit" disabled={disabled} className="dta-btn dta-btn--primary dta-btn--block" style={{ minHeight: 48 }}>{submitLabel}</button>
      {footer && <div style={{ fontSize: 15, textAlign: "center" }}>{footer}</div>}
    </form>
  );
}
