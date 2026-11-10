import { tokens } from "../../tokens.js";
import "./AuthLayout.css";

/**
 * Layout split-screen das telas de autenticação (login, cadastro).
 * Coluna esquerda: marca e mensagem institucional. Coluna direita: kicker, título,
 * aviso opcional (notice) e o formulário, passado em children.
 */
export function AuthLayout({ kicker, title, notice, children }) {
  return (
    <div className="dta dta-auth-layout">
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
          <p style={{ fontSize: 17, lineHeight: 1.55, color: "rgba(255, 255, 255, 0.82)", margin: 0 }}>
            Com uma conta de membro, você envia textos e PDFs, recebe a revisão e divulga seu trabalho.
          </p>
        </div>

        <div style={{ fontSize: 13.5, color: "rgba(255, 255, 255, 0.65)" }}>
          Ler continua livre, sem precisar de conta.
        </div>
      </section>

      <main className="dta-auth-form-panel">
        <div style={{ width: "100%", maxWidth: 440 }}>
          {kicker && (
            <div className="dta-kicker" style={{ marginBottom: 8 }}>
              {kicker}
            </div>
          )}
          <h2
            className="dta-serif"
            style={{ fontSize: 34, fontWeight: 700, margin: "0 0 28px", color: tokens.ink, lineHeight: 1.2 }}
          >
            {title}
          </h2>

          {notice && (
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
              {notice}
            </div>
          )}

          {children}
        </div>
      </main>
    </div>
  );
}
