import { useEffect, useRef, type RefObject } from 'react';

interface Props {
  folderName: string;
  onConfirm: () => void;
  onCancel: () => void;
  returnFocusRef?: RefObject<HTMLElement | null>;
}

/**
 * Inline, non-blocking delete confirmation (FR-6). Focus moves to Cancel (the safe default) on
 * open and returns to the trigger on close; Esc cancels.
 */
export function DeleteConfirm({ folderName, onConfirm, onCancel, returnFocusRef }: Props) {
  const cancelRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    cancelRef.current?.focus();
    return () => {
      returnFocusRef?.current?.focus();
    };
  }, [returnFocusRef]);

  return (
    <div
      className="confirm"
      role="alertdialog"
      aria-label={`Confirm delete folder ${folderName}`}
      aria-describedby="confirm-message"
      onKeyDown={(e) => {
        if (e.key === 'Escape') {
          e.stopPropagation();
          onCancel();
        }
      }}
    >
      <p id="confirm-message" className="confirm-message">
        Delete folder "{folderName}"? Tasks move to General.
      </p>
      <div className="confirm-actions">
        <button type="button" className="btn-danger" onClick={onConfirm}>
          Confirm
        </button>
        <button ref={cancelRef} type="button" className="btn-ghost" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </div>
  );
}
