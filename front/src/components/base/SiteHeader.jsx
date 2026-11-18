import { useEffect, useRef, useState } from "react";
import { tokens } from "../../tokens.js";
import { useNav } from "../../nav/NavContext.jsx";
import { useAuth } from "../../auth/AuthContext.jsx";
import { Logo } from "./Logo.jsx";
import { Button } from "./Button.jsx";
import { navParaUsuario } from "./defaultNav.js";

/**
 * Cabeçalho global. O usuário vem do AuthContext; o menu é montado pelo perfil.
 * nav: [{ label, href, current }] (opcional, substitui o padrão do perfil);
 * onSignIn, onSignUp: callbacks opcionais; progress: 0–100 (barra de leitura).
 * `user` ainda é aceito por compatibilidade e, se passado, tem precedência.
 */
export function SiteHeader({ user, nav, onSignIn, onSignUp, progress }) {
  const { Link, pathname, navigate } = useNav();
  const { usuario, sair } = useAuth();
  const atual = user !== undefined ? user : usuario;
  const itens = nav ?? navParaUsuario(atual);
  const [menuAberto, setMenuAberto] = useState(false);
  const [contaAberta, setContaAberta] = useState(false);
  const contaRef = useRef(null);

  // Fecha o menu da conta ao clicar fora ou pressionar Esc.
  useEffect(() => {
    if (!contaAberta) return undefined;
    const fora = (e) => { if (!contaRef.current?.contains(e.target)) setContaAberta(false); };
    const esc = (e) => { if (e.key === "Escape") setContaAberta(false); };
    document.addEventListener("mousedown", fora);
    document.addEventListener("keydown", esc);
    return () => {
      document.removeEventListener("mousedown", fora);
      document.removeEventListener("keydown", esc);
    };
  }, [contaAberta]);

  function entrar() {
    if (onSignIn) onSignIn();
    else navigate?.("/login");
  }

  function sairDaConta() {
    setContaAberta(false);
    setMenuAberto(false);
    sair();
    navigate?.("/");
  }

  const linkNav = (n, extra = {}) => {
    const current = n.current ?? (n.href === "/" ? pathname === "/" : pathname?.startsWith(n.href));
    return (
      <Link key={n.href + n.label} href={n.href} aria-current={current ? "page" : undefined}
            onClick={() => setMenuAberto(false)}
            style={{ color: current ? tokens.teal : tokens.ink, fontWeight: current ? 600 : 500, fontSize: 15, textDecoration: "none", ...extra }}>
        {n.label}
      </Link>
    );
  };

  const nomeExibido = atual?.nome || atual?.email;

  return (
    <header style={{ borderBottom: `1px solid ${tokens.lineSoft}`, position: "relative" }}>
      <div className="dta-wrap" style={{ display: "flex", flexWrap: "wrap", alignItems: "center", justifyContent: "space-between", gap: "16px 32px", padding: "20px 24px" }}>
        <Logo />
        <nav aria-label="Principal" className="dta-only-desktop" style={{ display: "flex", flexWrap: "wrap", gap: "8px 28px" }}>
          {itens.map((n) => linkNav(n))}
        </nav>
        <div className="dta-only-desktop" style={{ display: "flex", gap: 10, alignItems: "center" }}>
          {atual ? (
            <div ref={contaRef} style={{ position: "relative" }}>
              <button type="button" className="dta-btn" aria-haspopup="menu" aria-expanded={contaAberta}
                      onClick={() => setContaAberta((v) => !v)}>
                Olá, {nomeExibido}
              </button>
              {contaAberta && (
                <div role="menu" style={{ position: "absolute", right: 0, top: "calc(100% + 6px)", zIndex: 20, minWidth: 200,
                                          background: tokens.paper, border: `1px solid ${tokens.line}`, borderRadius: 4, padding: 6,
                                          display: "flex", flexDirection: "column", boxShadow: "0 6px 20px rgba(0,0,0,.08)" }}>
                  <Link role="menuitem" href="/conta" onClick={() => setContaAberta(false)}
                        style={{ padding: "10px 12px", color: tokens.ink, textDecoration: "none", fontSize: 15 }}>Minha conta</Link>
                  <button role="menuitem" type="button" onClick={sairDaConta}
                          style={{ padding: "10px 12px", textAlign: "left", background: "none", border: 0, font: "inherit", fontSize: 15, cursor: "pointer", color: tokens.ink }}>
                    Sair
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Button onClick={entrar}>Entrar</Button>
              <Link href="/cadastro" onClick={onSignUp} className="dta-btn dta-btn--primary"
                    style={{ display: "inline-flex", alignItems: "center", textDecoration: "none", color: "#fff" }}>Criar conta</Link>
            </>
          )}
        </div>
        <button type="button" className="dta-btn dta-only-mobile" aria-label={menuAberto ? "Fechar menu" : "Abrir menu"}
                aria-expanded={menuAberto} aria-controls="dta-menu-mobile" onClick={() => setMenuAberto((v) => !v)}
                style={{ width: 48, padding: 0, alignItems: "center", justifyContent: "center" }}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            {menuAberto ? <path d="M6 6l12 12M18 6L6 18" /> : <path d="M4 7h16M4 12h16M4 17h16" />}
          </svg>
        </button>
      </div>
      {menuAberto && (
        <nav id="dta-menu-mobile" aria-label="Menu" className="dta-only-mobile dta-wrap"
             style={{ display: "flex", flexDirection: "column", gap: 4, padding: "0 24px 20px" }}>
          {itens.map((n) => linkNav(n, { padding: "10px 0" }))}
          <div style={{ borderTop: `1px solid ${tokens.lineSoft}`, marginTop: 8, paddingTop: 12, display: "flex", flexDirection: "column", gap: 10 }}>
            {atual ? (
              <>
                <span className="dta-meta">Conectado como {nomeExibido}</span>
                {linkNav({ label: "Minha conta", href: "/conta" }, { padding: "10px 0" })}
                <Button onClick={sairDaConta}>Sair</Button>
              </>
            ) : (
              <>
                <Button onClick={() => { setMenuAberto(false); entrar(); }}>Entrar</Button>
                <Link href="/cadastro" onClick={() => setMenuAberto(false)} className="dta-btn dta-btn--primary"
                      style={{ display: "inline-flex", alignItems: "center", justifyContent: "center", textDecoration: "none", color: "#fff" }}>Criar conta</Link>
              </>
            )}
          </div>
        </nav>
      )}
      {typeof progress === "number" && (
        <div role="progressbar" aria-label="Progresso de leitura" aria-valuenow={progress} aria-valuemin={0} aria-valuemax={100}
             style={{ position: "absolute", left: 0, bottom: -1, height: 4, width: `${progress}%`, background: tokens.teal }} />
      )}
    </header>
  );
}
