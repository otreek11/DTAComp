export function Chip({ pressed = false, ...props }) {
  return <button type="button" className="dta-chip" aria-pressed={pressed} {...props} />;
}
