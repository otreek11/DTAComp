import { tokens } from "../../tokens.js";
import { Chip } from "../base/Chip.jsx";

/**
 * name, subtitle: autoria. rows: [{ label, value }]. tags: string[]; onTag(tag).
 * footer: node (ex.: link de denúncia).
 */
export function AboutWork({ label, name, subtitle, rows = [], tags = [], onTag, footer }) {
  return (
    <section aria-label={label} style={{ marginTop: 56, borderTop: `2px solid ${tokens.ink}`, paddingTop: 24, display: "flex", flexDirection: "column", gap: 20 }}>
      <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
        <div aria-hidden="true" style={{ width: 64, height: 64, borderRadius: "50%", background: tokens.tealSoft, flex: "none" }} />
        <div>
          <b className="dta-serif" style={{ fontSize: 20 }}>{name}</b>
          {subtitle && <div className="dta-meta" style={{ marginTop: 2 }}>{subtitle}</div>}
        </div>
      </div>
      {rows.length > 0 && (
        <div>{rows.map((r) => <div key={r.label} className="dta-row"><span>{r.label}</span><span>{r.value}</span></div>)}</div>
      )}
      {tags.length > 0 && (
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
          {tags.map((t) => <Chip key={t} onClick={() => onTag?.(t)}>{t}</Chip>)}
        </div>
      )}
      {footer && <div className="dta-meta">{footer}</div>}
    </section>
  );
}
