import { tokens } from "../../tokens.js";

/** items: [{ id, titulo, meta: string[], href }] */
export function RecentList({ title, items = [] }) {
  return (
    <aside aria-label={title} style={{ display: "flex", flexDirection: "column", gap: 20 }}>
      <span className="dta-kicker">{title}</span>
      {items.map((it) => (
        <a key={it.id} href={it.href} style={{ textDecoration: "none", color: "inherit", borderTop: `1px solid ${tokens.line}`, paddingTop: 16 }}>
          <div className="dta-serif" style={{ fontSize: 20, fontWeight: 500, lineHeight: 1.3 }}>{it.titulo}</div>
          {it.meta && <div className="dta-meta" style={{ marginTop: 6 }}>{it.meta.join(" · ")}</div>}
        </a>
      ))}
    </aside>
  );
}
