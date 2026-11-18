import { useCallback, useEffect, useMemo, useState } from "react";
import {
  LoginPage,
  SignUpPage,
  ExplorarPage,
  PublicarPage,
  RevisaoPage,
  ArtigoPage,
  MeusTrabalhosPage,
  ContaPage,
  AdminPage,
} from "./app/index.js";
import { NavProvider } from "./nav/NavProvider.jsx";
import { AuthProvider, useAuth } from "./auth/AuthContext.jsx";
import { SiteHeader } from "./components/base/SiteHeader.jsx";
import { SiteFooter } from "./components/base/SiteFooter.jsx";
import { EmptyState } from "./components/home/EmptyState.jsx";
import "./styles/dta.css";

/**
 * Rotas da SPA. `perfis`: quem pode acessar (ausente = público; [] = qualquer usuário logado).
 * "/explorar" é apelido de "/"; rotas desconhecidas caem em Explorar.
 */
const ROTAS = {
  "/": {},
  "/login": {},
  "/cadastro": {},
  "/artigo": {},
  "/publicar": { perfis: [] },
  "/dashboard": { perfis: [] },
  "/conta": { perfis: [] },
  "/revisao": { perfis: ["revisor"] },
  "/admin": { perfis: ["admin"] },
};

function lerLocalizacao() {
  if (typeof window === "undefined") return { rota: "/", artigoId: null, search: "", caminho: "/" };
  const { pathname, search } = window.location;
  const caminho = (pathname || "/").replace(/\/+$/, "") || "/";
  const artigo = caminho.match(/^\/artigo\/([^/]+)$/);
  let rota = caminho;
  if (caminho === "/explorar") rota = "/";
  else if (artigo) rota = "/artigo";
  else if (!ROTAS[caminho]) rota = "/";
  return { rota, artigoId: artigo ? decodeURIComponent(artigo[1]) : null, search, caminho: caminho + search };
}

function ehLinkInterno(a, e) {
  if (!a || e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return false;
  if (a.target && a.target !== "_self") return false;
  if (a.hasAttribute("download")) return false;
  const href = a.getAttribute("href");
  if (!href || !href.startsWith("/") || href.startsWith("//") || href.startsWith("/api/")) return false;
  return true;
}

function Redirecionar({ navigate, para }) {
  useEffect(() => {
    navigate(para, { replace: true });
  }, [navigate, para]);
  return null;
}

function AcessoRestrito({ navigate }) {
  return (
    <div className="dta" style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <SiteHeader />
      <main className="dta-wrap" style={{ flex: 1 }}>
        <EmptyState
          title="Acesso restrito"
          text="Seu perfil não tem permissão para acessar esta página."
          action={<button type="button" className="dta-btn dta-btn--primary" onClick={() => navigate("/")}>Voltar para Explorar</button>}
        />
      </main>
      <SiteFooter />
    </div>
  );
}

function Rotas() {
  const { usuario, pronto } = useAuth();
  const [local, setLocal] = useState(lerLocalizacao);

  const navigate = useCallback((path, { replace = false } = {}) => {
    if (typeof window !== "undefined" && window.history) {
      if (replace) window.history.replaceState({}, "", path);
      else window.history.pushState({}, "", path);
      window.scrollTo(0, 0);
    }
    setLocal(lerLocalizacao());
  }, []);

  // Voltar/avançar do navegador.
  useEffect(() => {
    const aoVoltar = () => setLocal(lerLocalizacao());
    window.addEventListener("popstate", aoVoltar);
    return () => window.removeEventListener("popstate", aoVoltar);
  }, []);

  // Qualquer <a href="/..."> da página navega sem recarregar.
  useEffect(() => {
    const aoClicar = (e) => {
      const a = e.target.closest?.("a[href]");
      if (!ehLinkInterno(a, e)) return;
      e.preventDefault();
      navigate(a.getAttribute("href"));
    };
    document.addEventListener("click", aoClicar);
    return () => document.removeEventListener("click", aoClicar);
  }, [navigate]);

  const { rota, artigoId, search, caminho } = local;
  const regra = ROTAS[rota] ?? {};
  const exigeLogin = Array.isArray(regra.perfis);

  const destinoAposLogin = () => {
    const next = new URLSearchParams(search).get("next");
    return next && next.startsWith("/") && !next.startsWith("//") ? next : "/";
  };

  // Componente estável: recriá-lo a cada render remontaria tudo que estiver dentro dos links.
  const Link = useMemo(
    () =>
      function LinkSpa({ href, onClick, children, ...rest }) {
        return (
          <a
            href={href}
            onClick={(e) => {
              if (onClick) onClick(e);
              if (!e.defaultPrevented && href && href.startsWith("/") && !href.startsWith("/api/")) {
                e.preventDefault();
                navigate(href);
              }
            }}
            {...rest}
          >
            {children}
          </a>
        );
      },
    [navigate],
  );

  let pagina;
  if (exigeLogin && !pronto) {
    pagina = <div className="dta" role="status" style={{ padding: 48, textAlign: "center" }}>Carregando…</div>;
  } else if (exigeLogin && !usuario) {
    // Rota protegida sem sessão → login, voltando depois para onde estava.
    pagina = <Redirecionar navigate={navigate} para={`/login?next=${encodeURIComponent(caminho)}`} />;
  } else if (regra.perfis?.length && !regra.perfis.includes(usuario.perfil)) {
    pagina = <AcessoRestrito navigate={navigate} />;
  } else {
    switch (rota) {
      case "/login":
        pagina = (
          <LoginPage
            onNavigateToSignUp={() => navigate("/cadastro")}
            onSuccess={() => navigate(destinoAposLogin(), { replace: true })}
          />
        );
        break;
      case "/cadastro":
        pagina = <SignUpPage onNavigateToLogin={() => navigate("/login")} onSuccess={() => navigate("/dashboard", { replace: true })} />;
        break;
      case "/artigo":
        pagina = <ArtigoPage id={artigoId} />;
        break;
      case "/publicar":
        pagina = <PublicarPage key={search} />;
        break;
      case "/dashboard":
        pagina = <MeusTrabalhosPage />;
        break;
      case "/conta":
        pagina = <ContaPage />;
        break;
      case "/revisao":
        pagina = <RevisaoPage />;
        break;
      case "/admin":
        pagina = <AdminPage />;
        break;
      default:
        // key: "/?tag=x" vindo do artigo recria a página com o filtro da URL.
        pagina = <ExplorarPage key={search} />;
    }
  }

  return (
    <NavProvider link={Link} pathname={rota === "/artigo" && artigoId ? `/artigo/${artigoId}` : rota} search={search} navigate={navigate}>
      {pagina}
    </NavProvider>
  );
}

function App() {
  return (
    <AuthProvider>
      <Rotas />
    </AuthProvider>
  );
}

export default App;
