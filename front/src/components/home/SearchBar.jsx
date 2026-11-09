import { tokens } from "../../tokens.js";

/** Busca global. Textos estáticos; valor e busca são controlados pela página. */
export function SearchBar({ id = "dta-q", value, onChange, onSearch }) {
  return (
    <form role="search" onSubmit={(e) => { e.preventDefault(); onSearch?.(value); }}
          style={{ display: "flex", flexWrap: "wrap", gap: 10, maxWidth: 760 }}>
      <label htmlFor={id} style={{ position: "absolute", left: -9999 }}>Buscar trabalhos</label>
      <input id={id} type="search" value={value} onChange={(e) => onChange?.(e.target.value)}
             placeholder="Buscar por título, autor ou palavra-chave"
             style={{ flex: 1, minWidth: 240, minHeight: 52, padding: "0 16px", font: `400 16px ${tokens.sans}`, border: `1px solid ${tokens.ink}`, borderRadius: 4 }} />
      <button type="submit" className="dta-btn dta-btn--primary" style={{ minHeight: 52 }}>Buscar</button>
    </form>
  );
}
