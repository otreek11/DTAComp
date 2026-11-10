import { Button } from "../base/Button.jsx";
import { ArticleCard } from "./ArticleCard.jsx";

/** empty: node exibido quando `items` está vazio. className: classes extras para a grade (ex.: variação responsiva). */
export function ArticleGrid({ items = [], empty = null, onOpen, hasMore, loadMoreLabel, onLoadMore, loadingMore = false, className = "" }) {
  if (!items.length) return empty;
  return (
    <>
      <div className={`dta-grid ${className}`.trim()}>
        {items.map((it) => <ArticleCard key={it.id} item={it} onOpen={onOpen} />)}
      </div>
      {hasMore && (
        <div className="dta-grid__more">
          <Button onClick={onLoadMore} disabled={loadingMore}>{loadingMore ? "Carregando..." : loadMoreLabel}</Button>
        </div>
      )}
    </>
  );
}
