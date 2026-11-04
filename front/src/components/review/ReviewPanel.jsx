import { tokens } from "../../tokens.js";
import { Button } from "../base/Button.jsx";

/**
 * header: { kicker, description } (opcional). children: sugestões, campo de parecer etc.
 * actions: [{ label, onClick, primary }].
 */
export function ReviewPanel({ label, header, actions = [], children }) {
  return (
    <aside aria-label={label} style={{ display: "flex", flexDirection: "column", gap: 16, minWidth: 0 }}>
      {header && (
        <div style={{ border: `1px solid ${tokens.line}`, borderRadius: 4, padding: 14, background: "#f6f8f8", fontSize: 14, display: "flex", flexDirection: "column", gap: 6 }}>
          <span className="dta-kicker">{header.kicker}</span>
          <span style={{ color: tokens.muted }}>{header.description}</span>
        </div>
      )}
      {children}
      {actions.length > 0 && (
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {actions.map((a) => <Button key={a.label} variant={a.primary ? "primary" : undefined} block onClick={a.onClick}>{a.label}</Button>)}
        </div>
      )}
    </aside>
  );
}
