/** title: texto do título (também rótulo acessível). headings: [{ id, label }]; activeId. */
export function TableOfContents({ title, headings = [], activeId }) {
  return (
    <nav className="dta-toc" aria-label={title}>
      <span className="dta-kicker" style={{ display: "block", marginBottom: 8 }}>{title}</span>
      {headings.map((h) => (
        <a key={h.id} href={`#${h.id}`} aria-current={h.id === activeId ? "true" : undefined}>{h.label}</a>
      ))}
    </nav>
  );
}
