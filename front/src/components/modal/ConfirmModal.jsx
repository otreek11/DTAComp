import { useId } from "react";
import { Button } from "../base/Button.jsx";
import { Modal } from "./Modal.jsx";

/**
 * Confirmação positiva. message/title vêm por prop; `confirmLabel` e `cancelLabel` têm
 * padrão. busy: desabilita ações enquanto a página processa.
 */
export function ConfirmModal({ open, onCancel, onConfirm, title, message, cancelLabel = "Cancelar", confirmLabel = "Confirmar", busy = false }) {
  const id = useId();
  return (
    <Modal open={open} onClose={busy ? undefined : onCancel} labelledBy={title ? `${id}-t` : `${id}-d`} describedBy={`${id}-d`} closeOnBackdrop={!busy}>
      <div className="dta-modal__body">
        {title && <h2 id={`${id}-t`} className="dta-serif dta-modal__title">{title}</h2>}
        <p id={`${id}-d`} className="dta-modal__text">{message}</p>
        <div className="dta-modal__actions">
          <Button onClick={onCancel} disabled={busy}>{cancelLabel}</Button>
          <Button variant="primary" onClick={onConfirm} disabled={busy} data-autofocus>{confirmLabel}</Button>
        </div>
      </div>
    </Modal>
  );
}
