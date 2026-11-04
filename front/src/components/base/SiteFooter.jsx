import { tokens } from "../../tokens.js";

/** Rodapé global, totalmente estático. */
export function SiteFooter() {
  const link = { color: "#e6e8eb" };
  return (
    <footer style={{ background: tokens.ink, color: "#e6e8eb", padding: "32px 24px", fontSize: 14 }}>
      <div className="dta-wrap" style={{ display: "flex", flexWrap: "wrap", gap: "16px 32px", justifyContent: "space-between", padding: 0 }}>
        <span>DTA-UFMA · Divulgação de Trabalhos Acadêmicos</span>
        <span style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
          <a href="/privacidade" style={link}>Privacidade e LGPD</a>
          <a href="/diretrizes" style={link}>Diretrizes de conteúdo</a>
          <a href="/acessibilidade" style={link}>Acessibilidade</a>
        </span>
      </div>
    </footer>
  );
}
