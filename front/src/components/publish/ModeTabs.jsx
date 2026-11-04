import { tokens } from "../../tokens.js";

/** modes: [{ id, label }]; label: rótulo acessível do grupo de abas. */
export function ModeTabs({ label, modes = [], active, onChange }) {
  return (
    <div role="tablist" aria-label={label} style={{ display: "flex" }}>
      {modes.map((m, i) => {
        const on = m.id === active;
        return (
          <button key={m.id} role="tab" type="button" aria-selected={on} onClick={() => onChange?.(m.id)}
                  className="dta-btn"
                  style={{ flex: 1, maxWidth: 220, borderRadius: i === 0 ? "4px 0 0 4px" : i === modes.length - 1 ? "0 4px 4px 0" : 0,
                           background: on ? tokens.ink : "#fff", color: on ? "#fff" : tokens.ink }}>
            {m.label}
          </button>
        );
      })}
    </div>
  );
}
