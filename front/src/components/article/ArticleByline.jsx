import { tokens } from "../../tokens.js";

/** Autoria no cabeçalho do artigo. name: string; meta: string[] (ex.: data, tempo de leitura). */
export function ArticleByline({ name, meta = [] }) {
  return (
    <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
      <div aria-hidden="true" style={{ width: 40, height: 40, borderRadius: "50%", background: tokens.tealSoft, flex: "none" }} />
      <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
        <b style={{ fontSize: 14 }}>{name}</b>
        {meta.length > 0 && <span className="dta-meta">{meta.join(" · ")}</span>}
      </div>
    </div>
  );
}
