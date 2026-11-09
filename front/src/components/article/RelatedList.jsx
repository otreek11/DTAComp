/** items: [{ id, kicker, titulo, meta: string[], href }] */
export function RelatedList({ title, items = [] }) {
  return (
    <section aria-label={title} style={{ marginTop: 72 }}>
      <h2 className="dta-serif" style={{ margin: "0 0 24px", fontSize: 28 }}>{title}</h2>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit,minmax(260px,1fr))", gap: 32 }}>
        {items.map((it) => (
          <article key={it.id} className="dta-card">
            {it.kicker && <span className="dta-kicker">{it.kicker}</span>}
            <h3 className="dta-serif"><a href={it.href} style={{ color: "inherit", textDecoration: "none" }}>{it.titulo}</a></h3>
            {it.meta && <span className="dta-meta">{it.meta.join(" · ")}</span>}
          </article>
        ))}
      </div>
    </section>
  );
}
