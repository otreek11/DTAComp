import { tokens } from "../../tokens.js";
import { Button } from "../base/Button.jsx";
import { ModeTabs } from "./ModeTabs.jsx";

/**
 * Estrutura do formulário de publicação. Campos vêm em children; estado, validação e
 * envio ficam na página.
 * kicker/title/description: cabeçalho. modes/mode/onModeChange/modesLabel: abas (opcional).
 * submitLabel + onSubmit; secondaryActions: [{ label, onClick }]; note: texto final.
 */
export function PublishForm({ ariaLabel, kicker, title, description, modes, modesLabel, mode, onModeChange,
                               onSubmit, submitLabel, secondaryActions = [], note, children }) {
  return (
    <section aria-label={ariaLabel} style={{ display: "flex", flexDirection: "column", gap: 24, minWidth: 0 }}>
      <div>
        {kicker && <span className="dta-kicker">{kicker}</span>}
        <h1 className="dta-serif" style={{ margin: "6px 0 0", fontSize: 40, lineHeight: 1.15 }}>{title}</h1>
        {description && <p style={{ margin: "8px 0 0", color: tokens.inkSoft, lineHeight: 1.55 }}>{description}</p>}
      </div>
      {modes && <ModeTabs label={modesLabel} modes={modes} active={mode} onChange={onModeChange} />}
      <form onSubmit={onSubmit} style={{ display: "flex", flexDirection: "column", gap: 20 }}>
        {children}
        <div style={{ display: "flex", flexWrap: "wrap", gap: 12 }}>
          <button type="submit" className="dta-btn dta-btn--primary">{submitLabel}</button>
          {secondaryActions.map((a) => <Button key={a.label} onClick={a.onClick}>{a.label}</Button>)}
        </div>
        {note && <p className="dta-meta" style={{ margin: 0 }} role="note">{note}</p>}
      </form>
    </section>
  );
}
