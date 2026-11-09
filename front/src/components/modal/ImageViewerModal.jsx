import { useRef } from "react";
import { Button } from "../base/Button.jsx";
import { Modal } from "./Modal.jsx";
import { useZoomPan } from "./useZoomPan.js";

const PAN = 60;

function Viewer({ src, alt, caption, onClose, zoomInLabel, zoomOutLabel, resetLabel, closeLabel }) {
  const stageRef = useRef(null);
  const z = useZoomPan(stageRef);

  function onKeyDown(e) {
    const keys = {
      "+": z.zoomIn, "=": z.zoomIn, "-": z.zoomOut, "0": z.reset,
      ArrowLeft: () => z.move(PAN, 0), ArrowRight: () => z.move(-PAN, 0),
      ArrowUp: () => z.move(0, PAN), ArrowDown: () => z.move(0, -PAN),
    };
    const action = keys[e.key];
    if (action && !(e.key.startsWith("Arrow") && e.target.tagName === "BUTTON" && z.scale === 1)) {
      e.preventDefault();
      action();
    }
  }

  return (
    <div className="dta-viewer" onKeyDown={onKeyDown}>
      <div className="dta-viewer__bar">
        <div className="dta-viewer__tools">
          <Button aria-label={zoomOutLabel} onClick={z.zoomOut} disabled={z.scale <= 1}>−</Button>
          <span className="dta-viewer__level" aria-live="polite">{Math.round(z.scale * 100)}%</span>
          <Button aria-label={zoomInLabel} onClick={z.zoomIn} disabled={z.scale >= 5}>+</Button>
          <Button onClick={z.reset} disabled={z.scale === 1}>{resetLabel}</Button>
        </div>
        <Button onClick={onClose}>{closeLabel}</Button>
      </div>
      <div ref={stageRef} className="dta-viewer__stage" data-dragging={z.dragging} data-autofocus tabIndex={0}
           onDoubleClick={z.toggle} {...z.handlers}>
        <img className="dta-viewer__img" src={src} alt={alt} draggable={false}
             style={{ transform: `translate(${z.x}px, ${z.y}px) scale(${z.scale})` }} />
      </div>
      {caption && <p className="dta-viewer__caption">{caption}</p>}
    </div>
  );
}

/**
 * Visualização de imagem em tela cheia com zoom (botões, roda, duplo clique, pinch, teclado)
 * e arraste. `alt` e `caption` vêm por prop; rótulos dos controles têm padrão.
 */
export function ImageViewerModal({ open, onClose, src, alt = "", caption, zoomInLabel = "Aumentar zoom", zoomOutLabel = "Diminuir zoom", resetLabel = "Ajustar", closeLabel = "Fechar" }) {
  return (
    <Modal open={open} onClose={onClose} size="full" ariaLabel={alt || caption || closeLabel}>
      <Viewer key={src} src={src} alt={alt} caption={caption} onClose={onClose}
              zoomInLabel={zoomInLabel} zoomOutLabel={zoomOutLabel} resetLabel={resetLabel} closeLabel={closeLabel} />
    </Modal>
  );
}
