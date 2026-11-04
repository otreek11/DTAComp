/** Rótulo + dica + erro ligados ao controle via aria. Passe o controle como children. */
export function Field({ label, hint, error, id, children }) {
  const describedBy = [hint && `${id}-hint`, error && `${id}-err`].filter(Boolean).join(" ") || undefined;
  return (
    <div className="dta-field">
      <label htmlFor={id}>{label}</label>
      {hint && <small id={`${id}-hint`}>{hint}</small>}
      {children({ id, "aria-describedby": describedBy, "aria-invalid": Boolean(error) })}
      {error && <span id={`${id}-err`} role="alert" className="dta-error">{error}</span>}
    </div>
  );
}
