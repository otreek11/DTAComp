import { tokens } from "../../tokens.js";
import { Chip } from "../base/Chip.jsx";

/**
 * title: título. tabs: [{ id, label }]; tab: id ativo; onTab(id).
 * queue: [{ id, titulo, meta }]; empty: node exibido com fila vazia.
 */
export function ReviewQueue({ label, title, tabs = [], tab, onTab, queue = [], selectedId, onSelect, empty = null }) {
  return (
    <nav aria-label={label} style={{ minWidth: 0 }}>
      <h1 className="dta-serif" style={{ margin: "0 0 12px", fontSize: 28 }}>{title}</h1>
      {tabs.length > 0 && (
        <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
          {tabs.map((t) => <Chip key={t.id} pressed={tab === t.id} onClick={() => onTab?.(t.id)}>{t.label}</Chip>)}
        </div>
      )}
      {queue.length === 0 && empty}
      {queue.map((q) => {
        const on = q.id === selectedId;
        return (
          <a key={q.id} href={q.href} onClick={(e) => { e.preventDefault(); onSelect?.(q.id); }} aria-current={on ? "true" : undefined}
             style={{ display: "flex", flexDirection: "column", gap: 4, padding: on ? "14px 16px 14px 12px" : "14px 16px", borderTop: `1px solid ${tokens.lineSoft}`,
                      borderLeft: on ? `4px solid ${tokens.teal}` : undefined, background: on ? "#eef3f4" : undefined, textDecoration: "none", color: "inherit" }}>
            <b className="dta-serif" style={{ fontWeight: 500, fontSize: 16 }}>{q.titulo}</b>
            {q.meta && <span className="dta-meta">{q.meta}</span>}
          </a>
        );
      })}
    </nav>
  );
}
