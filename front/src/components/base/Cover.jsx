import { tokens } from "../../tokens.js";

/** Imagem ou espaço reservado. `label` (opcional) é exibido só quando não há `src`. */
export function Cover({ src, alt = "", height = 160, label }) {
  if (src) return <img src={src} alt={alt} style={{ width: "100%", height, objectFit: "cover", display: "block" }} />;
  return (
    <div style={{ background: tokens.tealSoft, height, display: "flex", alignItems: "flex-end", padding: 12, fontSize: 12, color: "#2f4a4e" }}>
      {label}
    </div>
  );
}
