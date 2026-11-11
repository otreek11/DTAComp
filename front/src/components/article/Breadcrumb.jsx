import { useNav } from "../../nav/NavContext.jsx";

/** items: [{ label, href? }]. O último item é a página atual (sem link). */
export function Breadcrumb({ label = "Você está em", items = [] }) {
  const { Link } = useNav();
  return (
    <nav aria-label={label} className="dta-breadcrumb">
      <ol>
        {items.map((it, i) => {
          const ultimo = i === items.length - 1;
          return (
            <li key={it.label + i}>
              {it.href && !ultimo ? <Link href={it.href}>{it.label}</Link> : <span aria-current={ultimo ? "page" : undefined}>{it.label}</span>}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
