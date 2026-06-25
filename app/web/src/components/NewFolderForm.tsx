import { useState } from 'react';

interface Props {
  error: string | null;
  onSubmit: (name: string) => Promise<boolean>;
  onCancel: () => void;
}

/** Inline folder-creation form. Validation messages (empty/duplicate) come from the server. */
export function NewFolderForm({ error, onSubmit, onCancel }: Props) {
  const [name, setName] = useState('');

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const ok = await onSubmit(name);
    if (ok) {
      setName('');
    }
  };

  return (
    <form className="new-folder-form" onSubmit={submit}>
      <input
        type="text"
        className="new-folder-input"
        value={name}
        autoFocus
        placeholder="Folder name"
        aria-label="New folder name"
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? 'new-folder-error' : undefined}
        onChange={(e) => setName(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Escape') {
            onCancel();
          }
        }}
      />
      <div className="new-folder-actions">
        <button type="submit" className="btn-primary">Create</button>
        <button type="button" className="btn-ghost" onClick={onCancel}>Cancel</button>
      </div>
      {error && (
        <p id="new-folder-error" role="alert" className="field-error">
          {error}
        </p>
      )}
    </form>
  );
}
