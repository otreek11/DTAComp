import { tokens } from "../../tokens.js";
import { useNav } from "../../nav/NavContext.jsx";

/** Marca do site (estática). */
export function Logo({ href = "/" }) {
  const { Link } = useNav();
  return (
    <Link href={href} className="dta-serif" style={{ fontSize: 28, fontWeight: 700, color: tokens.ink, textDecoration: "none" }}>
      DTA<span style={{ color: tokens.teal }}>·UFMA</span>
    </Link>
  );
}
