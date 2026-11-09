import { tokens } from "../../tokens.js";
import { Button } from "../base/Button.jsx";

export function ReturnedNotice({ kicker, titulo, description, actionLabel, onAction }) {
  return (
    <div style={{ border: `1px solid ${tokens.ink}`, borderRadius: 4, padding: 16 }}>
      {kicker && <span className="dta-kicker" style={{ color: tokens.warn }}>{kicker}</span>}
      <div className="dta-serif" style={{ fontSize: 18, fontWeight: 500, margin: "6px 0" }}>{titulo}</div>
      {description && <p style={{ margin: "0 0 12px", fontSize: 14, lineHeight: 1.5, color: tokens.inkSoft }}>{description}</p>}
      {actionLabel && <Button onClick={onAction}>{actionLabel}</Button>}
    </div>
  );
}
