import { useId } from "react";
import { Button } from "../base/Button.jsx";
import { Modal } from "./Modal.jsx";

/**
 * Confirmação negativa (ação destrutiva). `message` (aviso) e `confirmLabel` vêm por prop.
 * O foco inicial fica em "Cancelar".
 */
export function DangerConfirmModal({ open, onCancel, onConfirm, title, message, cancelLabel = "Cancelar", confirmLabel, busy = false }) {
  const id = useId();
  return (
    <Modal open={open} onClose={busy ? undefined : onCancel} role="alertdialog" labelledBy={title ? `${id}-t` : `${id}-d`} describedBy={`${id}-d`} closeOnBackdrop={!busy}>
      <div className="dta-modal__body">
        {title && <h2 id={`${id}-t`} className="dta-serif dta-modal__title">{title}</h2>}
        <p id={`${id}-d`} className="dta-modal__text">{message}</p>
        <div className="dta-modal__actions">
          <Button onClick={onCancel} disabled={busy} data-autofocus>{cancelLabel}</Button>
          <Button variant="danger" onClick={onConfirm} disabled={busy}>{confirmLabel}</Button>
        </div>
      </div>
    </Modal>
  );
}
