import { Cover } from "../base/Cover.jsx";

/**
 * blocks: [{ type: "p" | "h2" | "quote" | "image", text?, src?, alt?, caption?, id? }]
 * Renderiza igual para qualquer origem de conteúdo.
 * renderText(bloco) (opcional): devolve o conteúdo de texto do bloco (ex.: com trechos
 * destacados). Sem ela, o texto é exibido como está.
 * onImageClick(bloco) (opcional): torna as imagens clicáveis (ex.: abrir o visualizador com zoom).
 */
export function ArticleBody({ blocks = [], renderText, onImageClick }) {
  const texto = (b) => (renderText ? renderText(b) : b.text);
  return (
    <div className="dta-prose">
      {blocks.map((b, i) => {
        if (b.type === "h2") return <h2 key={i} id={b.id}>{texto(b)}</h2>;
        if (b.type === "quote") return <blockquote key={i}>{texto(b)}</blockquote>;
        if (b.type === "image") {
          return (
            <figure key={i} style={{ margin: "0 0 32px" }}>
              {onImageClick ? (
                <button type="button" onClick={() => onImageClick(b)} aria-label={`Ampliar imagem: ${b.alt || b.caption || ""}`}
                        style={{ display: "block", width: "100%", padding: 0, border: 0, background: "none", cursor: "zoom-in" }}>
                  <Cover src={b.src} alt={b.alt} height={b.src ? "auto" : 340} label="Imagem" />
                </button>
              ) : (
                <Cover src={b.src} alt={b.alt} height={340} label="Imagem" />
              )}
              {b.caption && <figcaption className="dta-meta" style={{ marginTop: 8 }}>{b.caption}</figcaption>}
            </figure>
          );
        }
        return <p key={i}>{texto(b)}</p>;
      })}
    </div>
  );
}
