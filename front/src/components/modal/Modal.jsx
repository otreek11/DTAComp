import { useEffect, useRef } from "react";
import { createPortal } from "react-dom";

/**
 * Casca acessível baseada em <dialog>. Controlada: a página guarda `open`.
 * Foco preso, Esc e inércia do fundo vêm do navegador (showModal). O foco inicial vai
 * para o elemento com `data-autofocus` (ou o primeiro focável). Os filhos só são
 * montados enquanto aberto, então o estado interno é reiniciado a cada abertura.
 * size: "sm" | "lg" | "full". role: "dialog" | "alertdialog".
 */
export function Modal({ open, onClose, ariaLabel, labelledBy, describedBy, role = "dialog", size = "sm", closeOnBackdrop = true, children }) {
  const ref = useRef(null);

  useEffect(() => {
    const d = ref.current;
    if (!d) return;
    if (open && !d.open) {
      d.showModal();
      d.querySelector("[data-autofocus]")?.focus();
    } else if (!open && d.open) {
      d.close();
    }
  }, [open]);

  useEffect(() => {
    if (!open) return undefined;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => { document.body.style.overflow = prev; };
  }, [open]);

  return createPortal(
    <dialog
      ref={ref}
      className={`dta dta-modal dta-modal--${size}`}
      role={role}
      aria-label={ariaLabel}
      aria-labelledby={labelledBy}
      aria-describedby={describedBy}
      onCancel={(e) => { e.preventDefault(); onClose?.(); }}
      onClick={(e) => { if (closeOnBackdrop && e.target === e.currentTarget) onClose?.(); }}
    >
      {open && children}
    </dialog>,
    document.body,
  );
}
