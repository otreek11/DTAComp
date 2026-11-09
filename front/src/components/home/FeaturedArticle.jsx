import { tokens } from "../../tokens.js";
import { Cover } from "../base/Cover.jsx";

/** item: { titulo, resumo, kicker, meta: string[], capa, capaAlt } */
export function FeaturedArticle({ item }) {
  return (
    <article style={{ minWidth: 0, display: "flex", flexDirection: "column", gap: 14 }}>
      <Cover src={item.capa} alt={item.capaAlt} height={340} />
      {item.kicker && <span className="dta-kicker">{item.kicker}</span>}
      <h2 className="dta-serif" style={{ margin: 0, fontSize: 36, lineHeight: 1.15 }}>{item.titulo}</h2>
      {item.resumo && <p style={{ margin: 0, fontSize: 17, lineHeight: 1.55, color: tokens.inkSoft }}>{item.resumo}</p>}
      {item.meta && <span className="dta-meta">{item.meta.join(" · ")}</span>}
    </article>
  );
}
