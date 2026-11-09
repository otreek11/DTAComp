import { Cover } from "../base/Cover.jsx";

/**
 * blocks: [{ type: "p" | "h2" | "quote" | "image", text?, src?, alt?, caption?, id? }]
 * Renderiza igual para qualquer origem de conteúdo.
 */
export function ArticleBody({ blocks = [] }) {
  return (
    <div className="dta-prose">
      {blocks.map((b, i) => {
        if (b.type === "h2") return <h2 key={i} id={b.id}>{b.text}</h2>;
        if (b.type === "quote") return <blockquote key={i}>{b.text}</blockquote>;
        if (b.type === "image") {
          return (
            <figure key={i} style={{ margin: "0 0 32px" }}>
              <Cover src={b.src} alt={b.alt} height={340} label="Imagem" />
              {b.caption && <figcaption className="dta-meta" style={{ marginTop: 8 }}>{b.caption}</figcaption>}
            </figure>
          );
        }
        return <p key={i}>{b.text}</p>;
      })}
    </div>
  );
}
