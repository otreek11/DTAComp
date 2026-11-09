import { tokens } from "../../tokens.js";

/** steps: [{ label, done, note }] — `note` (opcional) é exibido ao lado do rótulo. */
export function WorkStatusCard({ titulo, subtitle, steps = [] }) {
  return (
    <div style={{ border: `1px solid ${tokens.line}`, borderRadius: 4, padding: 16 }}>
      <div className="dta-serif" style={{ fontSize: 18, fontWeight: 500 }}>{titulo}</div>
      {subtitle && <div className="dta-meta" style={{ margin: "4px 0 12px" }}>{subtitle}</div>}
      {steps.map((s) => (
        <div key={s.label} style={{ display: "flex", alignItems: "center", gap: 10, fontSize: 14, padding: "10px 0", borderTop: `1px solid ${tokens.lineSoft}` }}>
          <span aria-hidden="true" style={{ width: 12, height: 12, borderRadius: "50%", background: s.done ? tokens.teal : tokens.line, flex: "none" }} />
          {s.label}{s.note ? ` ${s.note}` : ""}
        </div>
      ))}
    </div>
  );
}
