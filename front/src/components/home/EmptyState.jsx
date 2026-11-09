import { tokens } from "../../tokens.js";

/** title, text: textos; action: node opcional (ex.: botão). */
export function EmptyState({ title, text, action }) {
  return (
    <div style={{ padding: "48px 0", textAlign: "center" }}>
      <h3 className="dta-serif" style={{ margin: "0 0 8px", fontSize: 24 }}>{title}</h3>
      {text && <p style={{ margin: "0 0 16px", color: tokens.inkSoft }}>{text}</p>}
      {action}
    </div>
  );
}
