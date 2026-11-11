import { tokens } from "../../tokens.js";
import { Button } from "../base/Button.jsx";

/**
 * sugestao: { categoria, trecho }. Rótulos e callbacks vêm por props.
 * Opcionais (revisão): status ("aceita" | "ignorada") + statusLabel (texto do estado),
 * active (card ligado ao trecho em destaque), onSelect(sugestao) (clique ou foco no card),
 * readOnly (desabilita as decisões).
 */
export function AiSuggestionCard({ sugestao, acceptLabel, ignoreLabel, onAccept, onIgnore, status, statusLabel, active = false, onSelect, readOnly = false }) {
  return (
    <div
      onClick={() => onSelect?.(sugestao)}
      onFocus={() => onSelect?.(sugestao)}
      style={{ border: `1px solid ${active ? tokens.teal : tokens.line}`, boxShadow: active ? `0 0 0 1px ${tokens.teal}` : undefined, borderRadius: 4, padding: 14, display: "flex", flexDirection: "column", gap: 6, fontSize: 14, lineHeight: 1.5 }}
    >
      <b>{sugestao.categoria}</b>
      <span>{sugestao.trecho}</span>
      {status && statusLabel && <span className="dta-meta">{statusLabel}</span>}
      <div style={{ display: "flex", gap: 8 }}>
        {acceptLabel && (
          <Button variant={status === "aceita" ? "primary" : undefined} aria-pressed={status ? status === "aceita" : undefined} disabled={readOnly} onClick={() => onAccept?.(sugestao)}>{acceptLabel}</Button>
        )}
        {ignoreLabel && (
          <Button aria-pressed={status ? status === "ignorada" : undefined} disabled={readOnly} style={status === "ignorada" ? { background: tokens.ink, color: "#fff" } : undefined} onClick={() => onIgnore?.(sugestao)}>{ignoreLabel}</Button>
        )}
      </div>
    </div>
  );
}
