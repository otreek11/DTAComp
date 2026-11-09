import { Button } from "../base/Button.jsx";

/** commands: [{ id, label (texto ou node), ariaLabel }]; onCommand(id). */
export function EditorToolbar({ label, commands = [], onCommand }) {
  return (
    <div role="toolbar" aria-label={label} style={{ display: "flex", gap: 6, overflowX: "auto" }}>
      {commands.map((c) => (
        <Button key={c.id} aria-label={c.ariaLabel} onClick={() => onCommand?.(c.id)} style={{ padding: "0 14px" }}>{c.label}</Button>
      ))}
    </div>
  );
}
