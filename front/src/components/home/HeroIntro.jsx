import { tokens } from "../../tokens.js";

/** title, description: textos. children: conteúdo extra (ex.: SearchBar). */
export function HeroIntro({ title, description, children }) {
  return (
    <section style={{ padding: "32px 0 40px" }}>
      <h1 className="dta-serif" style={{ margin: "0 0 12px", fontSize: "clamp(34px,5vw,56px)", lineHeight: 1.1, maxWidth: 820 }}>{title}</h1>
      {description && <p style={{ margin: "0 0 24px", fontSize: 18, lineHeight: 1.55, color: tokens.inkSoft, maxWidth: 640 }}>{description}</p>}
      {children}
    </section>
  );
}
