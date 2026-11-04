import { Button } from "../base/Button.jsx";

/**
 * Ferramentas de leitura (textos estáticos). Comportamentos vêm por callbacks;
 * o pai aplica `--dta-font` e a classe `dta-hc` no contêiner.
 */
export function ReaderTools({ onShare, onCite, onFontSize, onContrast }) {
  return (
    <>
      <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
        <Button block onClick={onShare}>Compartilhar</Button>
        <Button block onClick={onCite}>Citar</Button>
      </div>
      <div>
        <span className="dta-kicker">Leitura</span>
        <div style={{ display: "flex", gap: 8, marginTop: 10, flexWrap: "wrap" }}>
          <Button aria-label="Diminuir fonte" onClick={() => onFontSize?.(-2)}>A−</Button>
          <Button aria-label="Aumentar fonte" onClick={() => onFontSize?.(2)}>A+</Button>
          <Button onClick={onContrast}>Contraste</Button>
        </div>
      </div>
    </>
  );
}
