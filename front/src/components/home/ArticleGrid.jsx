import { Button } from "../base/Button.jsx";
import { ArticleCard } from "./ArticleCard.jsx";

/** empty: node exibido quando `items` está vazio. */
export function ArticleGrid({ items = [], empty = null, onOpen, hasMore, loadMoreLabel, onLoadMore }) {
  if (!items.length) return empty;
  return (
    <>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill,minmax(280px,1fr))", gap: "40px 32px" }}>
        {items.map((it) => <ArticleCard key={it.id} item={it} onOpen={onOpen} />)}
      </div>
      {hasMore && <div style={{ textAlign: "center", marginTop: 48 }}><Button onClick={onLoadMore}>{loadMoreLabel}</Button></div>}
    </>
  );
}
