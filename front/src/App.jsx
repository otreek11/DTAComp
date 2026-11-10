import { useState, useEffect } from "react";
import { LoginPage, SignUpPage, ExplorarPage } from "./app/index.js";
import { NavProvider } from "./nav/NavProvider.jsx";
import "./styles/dta.css";

const ROTAS = ["/", "/login", "/cadastro", "/dashboard"];

/** "/explorar" é um apelido de "/"; rotas desconhecidas caem na página Explorar. */
function normalizarRota(pathname) {
  const rota = (pathname || "/").replace(/\/+$/, "") || "/";
  if (rota === "/explorar") return "/";
  return ROTAS.includes(rota) ? rota : "/";
}

function App() {
  const [currentRoute, setCurrentRoute] = useState(() =>
    normalizarRota(typeof window !== "undefined" ? window.location.pathname : "/")
  );

  const [usuarioLogado, setUsuarioLogado] = useState(null);

  useEffect(() => {
    const handlePopState = () => {
      setCurrentRoute(normalizarRota(window.location.pathname));
    };
    window.addEventListener("popstate", handlePopState);
    return () => window.removeEventListener("popstate", handlePopState);
  }, []);

  function navigateTo(path) {
    if (typeof window !== "undefined" && window.history?.pushState) {
      window.history.pushState({}, "", path);
    }
    setCurrentRoute(normalizarRota(path));
  }

  const customLink = ({ href, onClick, children, ...rest }) => (
    <a
      href={href}
      onClick={(e) => {
        if (onClick) onClick(e);
        if (!e.defaultPrevented && href && href.startsWith("/")) {
          e.preventDefault();
          navigateTo(href);
        }
      }}
      {...rest}
    >
      {children}
    </a>
  );

  return (
    <NavProvider link={customLink} pathname={currentRoute}>
      {currentRoute === "/" && (
        <ExplorarPage
          user={usuarioLogado ? { nome: usuarioLogado.nome || usuarioLogado.email } : null}
          onSignIn={() => navigateTo("/login")}
        />
      )}

      {currentRoute === "/cadastro" && (
        <SignUpPage
          onNavigateToLogin={() => navigateTo("/login")}
          onSuccess={() => navigateTo("/dashboard")}
        />
      )}

      {currentRoute === "/login" && (
        <LoginPage
          onNavigateToSignUp={() => navigateTo("/cadastro")}
          onSuccess={(user) => {
            setUsuarioLogado(user);
            navigateTo("/");
          }}
        />
      )}

      {currentRoute === "/dashboard" && (
        <div
          className="dta"
          style={{
            minHeight: "100vh",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            gap: 16,
            padding: 24,
            textAlign: "center",
          }}
        >
          <div className="dta-kicker">PAINEL DO MEMBRO</div>
          <h2 className="dta-serif" style={{ fontSize: 32, margin: 0 }}>
            Bem-vindo(a) ao DTA · UFMA!
          </h2>
          {usuarioLogado && (
            <p style={{ margin: 0, color: "var(--dta-muted)" }}>
              Autenticado como: <strong>{usuarioLogado.email}</strong> ({usuarioLogado.perfil})
            </p>
          )}
          <p style={{ maxWidth: 520, lineHeight: 1.55, margin: 0, color: "var(--dta-ink-soft)" }}>
            Você já tem acesso à submissão de trabalhos acadêmicos, visualização de revisões e divulgação de pesquisas.
          </p>
          <div style={{ display: "flex", gap: 12, marginTop: 12 }}>
            <button
              type="button"
              className="dta-btn"
              onClick={() => {
                localStorage.removeItem("dtacomp_token");
                localStorage.removeItem("dtacomp_user");
                setUsuarioLogado(null);
                navigateTo("/login");
              }}
            >
              Sair da conta
            </button>
            <button
              type="button"
              className="dta-btn dta-btn--primary"
              onClick={() => navigateTo("/cadastro")}
            >
              Ir para Cadastro
            </button>
          </div>
        </div>
      )}
    </NavProvider>
  );
}

export default App;
