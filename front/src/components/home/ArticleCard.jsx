import { Cover } from "../base/Cover.jsx";

/** item: { titulo, resumo, kicker, meta: string[], capa, capaAlt, href } */
export function ArticleCard({ item, onOpen }) {
  return (
    <article className="dta-card">
      <Cover src={item.capa} alt={item.capaAlt} />
      {item.kicker && <span className="dta-kicker">{item.kicker}</span>}
      <h3 className="dta-serif"><a href={item.href} onClick={() => onOpen?.(item)} style={{ color: "inherit", textDecoration: "none" }}>{item.titulo}</a></h3>
      {item.resumo && <p>{item.resumo}</p>}
      {item.meta && <span className="dta-meta">{item.meta.join(" · ")}</span>}
    </article>
  );
}
