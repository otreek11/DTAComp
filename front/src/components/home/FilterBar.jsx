import { Chip } from "../base/Chip.jsx";

/** options: string[]; active: string; onChange(opt); label: rótulo acessível do grupo. */
export function FilterBar({ label, options = [], active, onChange }) {
  return (
    <div role="group" aria-label={label} style={{ display: "flex", flexWrap: "wrap", gap: 10, overflowX: "auto" }}>
      {options.map((o) => (
        <Chip key={o} pressed={active === o} onClick={() => onChange?.(o)}>{o}</Chip>
      ))}
    </div>
  );
}
