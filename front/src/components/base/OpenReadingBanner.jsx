import { tokens } from "../../tokens.js";
import { useNav } from "../../nav/NavContext.jsx";

/** Faixa que reforça que ler não exige conta (RF07). Texto estático. */
export function OpenReadingBanner({ onSignUp }) {
  const { Link } = useNav();
  return (
    <div style={{ background: tokens.teal, color: "#fff", fontSize: 14, padding: "10px 24px", textAlign: "center" }}>
      Leitura aberta a todos: você não precisa de conta para ler. Para publicar,{" "}
      <Link href="/cadastro" onClick={onSignUp} style={{ color: "#fff", fontWeight: 600 }}>crie sua conta</Link>.
    </div>
  );
}
