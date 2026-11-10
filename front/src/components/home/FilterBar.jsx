import { Chip } from "../base/Chip.jsx";

/**
 * options: string[] ou [{ value, label }]; active: valor ativo; onChange(valor);
 * label: rótulo acessível do grupo; className: classes extras (ex.: rolagem horizontal no mobile).
 */
export function FilterBar({ label, options = [], active, onChange, className = "" }) {
  return (
    <div role="group" aria-label={label} className={`dta-filterbar ${className}`.trim()}>
      {options.map((o) => {
        const value = typeof o === "string" ? o : o.value;
        const text = typeof o === "string" ? o : o.label;
        return <Chip key={value} pressed={active === value} onClick={() => onChange?.(value)}>{text}</Chip>;
      })}
    </div>
  );
}
