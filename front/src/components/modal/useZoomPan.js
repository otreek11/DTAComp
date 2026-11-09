import { useCallback, useEffect, useRef, useState } from "react";

const MIN = 1;
const MAX = 5;
const STEP = 0.5;
const clamp = (v, a, b) => Math.min(b, Math.max(a, v));

/**
 * Zoom (1×–5×) e deslocamento de uma imagem dentro de `stageRef`.
 * Retorna o estado, ações e handlers de ponteiro (arrastar e pinch). A roda do mouse
 * é ligada ao elemento com listener não passivo.
 */
export function useZoomPan(stageRef) {
  const [view, setView] = useState({ scale: 1, x: 0, y: 0 });
  const [dragging, setDragging] = useState(false);
  const viewRef = useRef(view);
  const pointers = useRef(new Map());
  const gesture = useRef(null);

  useEffect(() => { viewRef.current = view; });

  const limit = useCallback((scale, x, y) => {
    const el = stageRef.current;
    const mx = ((scale - 1) * (el?.clientWidth ?? 0)) / 2;
    const my = ((scale - 1) * (el?.clientHeight ?? 0)) / 2;
    return { scale, x: clamp(x, -mx, mx), y: clamp(y, -my, my) };
  }, [stageRef]);

  const zoomTo = useCallback((next) => setView((v) => {
    const s = clamp(next(v.scale), MIN, MAX);
    return s === MIN ? { scale: 1, x: 0, y: 0 } : limit(s, v.x, v.y);
  }), [limit]);

  const zoomIn = useCallback(() => zoomTo((s) => s + STEP), [zoomTo]);
  const zoomOut = useCallback(() => zoomTo((s) => s - STEP), [zoomTo]);
  const reset = useCallback(() => setView({ scale: 1, x: 0, y: 0 }), []);
  const toggle = useCallback(() => zoomTo((s) => (s > 1 ? 1 : 2)), [zoomTo]);
  const move = useCallback((dx, dy) => setView((v) => (v.scale > 1 ? limit(v.scale, v.x + dx, v.y + dy) : v)), [limit]);

  useEffect(() => {
    const el = stageRef.current;
    if (!el) return undefined;
    const onWheel = (e) => {
      e.preventDefault();
      zoomTo((s) => s * Math.exp(-e.deltaY * (e.ctrlKey ? 0.006 : 0.0015)));
    };
    el.addEventListener("wheel", onWheel, { passive: false });
    return () => el.removeEventListener("wheel", onWheel);
  }, [stageRef, zoomTo]);

  const startPan = (p) => {
    const v = viewRef.current;
    gesture.current = v.scale > 1 ? { type: "pan", sx: p.x, sy: p.y, ox: v.x, oy: v.y } : null;
  };

  const handlers = {
    onPointerDown(e) {
      e.currentTarget.setPointerCapture(e.pointerId);
      pointers.current.set(e.pointerId, { x: e.clientX, y: e.clientY });
      if (pointers.current.size === 2) {
        const [a, b] = [...pointers.current.values()];
        gesture.current = { type: "pinch", dist: Math.hypot(a.x - b.x, a.y - b.y) || 1, scale: viewRef.current.scale };
      } else {
        startPan({ x: e.clientX, y: e.clientY });
      }
      setDragging(true);
    },
    onPointerMove(e) {
      if (!pointers.current.has(e.pointerId)) return;
      pointers.current.set(e.pointerId, { x: e.clientX, y: e.clientY });
      const g = gesture.current;
      if (!g) return;
      if (g.type === "pinch" && pointers.current.size === 2) {
        const [a, b] = [...pointers.current.values()];
        const ratio = Math.hypot(a.x - b.x, a.y - b.y) / g.dist;
        zoomTo(() => g.scale * ratio);
      } else if (g.type === "pan") {
        setView((v) => limit(v.scale, g.ox + e.clientX - g.sx, g.oy + e.clientY - g.sy));
      }
    },
    onPointerUp(e) {
      pointers.current.delete(e.pointerId);
      const rest = [...pointers.current.values()];
      if (rest.length === 1) startPan(rest[0]);
      else gesture.current = null;
      if (rest.length === 0) setDragging(false);
    },
  };
  handlers.onPointerCancel = handlers.onPointerUp;

  return { ...view, dragging, zoomIn, zoomOut, reset, toggle, move, handlers };
}
