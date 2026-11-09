import { tokens } from "../../tokens.js";
import { useNav } from "../../nav/NavContext.jsx";
import { Logo } from "./Logo.jsx";
import { Button } from "./Button.jsx";
import { defaultNav } from "./defaultNav.js";


/**
 * Cabeçalho global. Textos fixos; a página controla só os dados e comportamentos:
 * user: null (visitante) ou { nome }; nav: [{ label, href, current }] (opcional, substitui o padrão);
 * onSignIn, onSignUp, onMenu, onAccessibility: callbacks; progress: 0–100 (barra de leitura).
 */
export function SiteHeader({ user = null, nav = defaultNav, onSignIn, onSignUp, onMenu, onAccessibility, progress }) {
  const { Link, pathname } = useNav();
  return (
    <header style={{ borderBottom: `1px solid ${tokens.lineSoft}`, position: "relative" }}>
      <div className="dta-wrap" style={{ display: "flex", flexWrap: "wrap", alignItems: "center", justifyContent: "space-between", gap: "16px 32px", padding: "20px 24px" }}>
        <Logo />
        <nav aria-label="Principal" className="dta-only-desktop" style={{ display: "flex", flexWrap: "wrap", gap: "8px 28px" }}>
          {nav.map((n) => {
            const current = n.current ?? n.href === pathname;
            return (
              <Link key={n.href + n.label} href={n.href} aria-current={current ? "page" : undefined}
                    style={{ color: current ? tokens.teal : tokens.ink, fontWeight: current ? 600 : 500, fontSize: 15, textDecoration: "none" }}>
                {n.label}
              </Link>
            );
          })}
        </nav>
        <div className="dta-only-desktop" style={{ display: "flex", gap: 10, alignItems: "center" }}>
          {onAccessibility && <Button aria-label="Opções de acessibilidade: fonte e contraste" onClick={onAccessibility}>Aa</Button>}
          {user ? <span style={{ fontSize: 14 }}>Olá, {user.nome}</span> : (
            <>
              <Button onClick={onSignIn}>Entrar</Button>
              <Link href="/cadastro" onClick={onSignUp} className="dta-btn dta-btn--primary"
                    style={{ display: "inline-flex", alignItems: "center", textDecoration: "none", color: "#fff" }}>Criar conta</Link>
            </>
          )}
        </div>
        <button type="button" className="dta-btn dta-only-mobile" aria-label="Abrir menu" onClick={onMenu}
                style={{ width: 48, padding: 0, alignItems: "center", justifyContent: "center" }}>
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16" /></svg>
        </button>
      </div>
      {typeof progress === "number" && (
        <div role="progressbar" aria-label="Progresso de leitura" aria-valuenow={progress} aria-valuemin={0} aria-valuemax={100}
             style={{ position: "absolute", left: 0, bottom: -1, height: 4, width: `${progress}%`, background: tokens.teal }} />
      )}
    </header>
  );
}
