import { tokens } from "../../tokens.js";
import { Button } from "../base/Button.jsx";

/** sugestao: { categoria, trecho }. Rótulos e callbacks vêm por props. */
export function AiSuggestionCard({ sugestao, acceptLabel, ignoreLabel, onAccept, onIgnore }) {
  return (
    <div style={{ border: `1px solid ${tokens.line}`, borderRadius: 4, padding: 14, display: "flex", flexDirection: "column", gap: 6, fontSize: 14, lineHeight: 1.5 }}>
      <b>{sugestao.categoria}</b>
      <span>{sugestao.trecho}</span>
      <div style={{ display: "flex", gap: 8 }}>
        {acceptLabel && <Button onClick={() => onAccept?.(sugestao)}>{acceptLabel}</Button>}
        {ignoreLabel && <Button onClick={() => onIgnore?.(sugestao)}>{ignoreLabel}</Button>}
      </div>
    </div>
  );
}
