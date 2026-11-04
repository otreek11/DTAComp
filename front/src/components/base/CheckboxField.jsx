/** label: node (pode conter links). error: texto do erro, se houver. */
export function CheckboxField({ id, label, checked, onChange, error }) {
  const errId = error ? `${id}-err` : undefined;
  return (
    <div>
      <label htmlFor={id} style={{ display: "flex", gap: 12, alignItems: "flex-start", fontSize: 14, lineHeight: 1.5, minHeight: 44 }}>
        <input id={id} type="checkbox" checked={checked} onChange={onChange} aria-invalid={Boolean(error)} aria-describedby={errId}
               style={{ width: 24, height: 24, flex: "none", margin: 0 }} />
        <span>{label}</span>
      </label>
      {error && <span id={errId} role="alert" className="dta-error" style={{ color: "var(--dta-warn)", fontSize: 14 }}>{error}</span>}
    </div>
  );
}
