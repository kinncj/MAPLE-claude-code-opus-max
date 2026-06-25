import { useRef } from 'react';
import { useTodo } from '../state/TodoProvider';
import { activeFolder } from '../state/selectors';
import { AddTask } from './AddTask';
import { FilterPills } from './FilterPills';
import { TaskList } from './TaskList';
import { DeleteConfirm } from './DeleteConfirm';

export function RightPanel() {
  const { state, actions } = useTodo();
  const deleteBtnRef = useRef<HTMLButtonElement>(null);

  const active = activeFolder(state.folders, state.selection);
  // Delete is hidden for General (default) and in the All-folders view (FR-4, FR-11).
  const showDelete = active !== null && !active.isDefault;
  const confirmingFolder =
    state.folders.find((f) => f.id === state.confirmingDeleteFolderId) ?? null;

  return (
    <main className="right-panel">
      <AddTask />

      <div className="toolbar">
        <FilterPills value={state.filter} onChange={actions.setFilter} />
        {showDelete && (
          <button
            ref={deleteBtnRef}
            type="button"
            className="del-folder"
            aria-haspopup="dialog"
            aria-label={`Delete folder ${active.name}`}
            onClick={() => actions.requestDeleteFolder(active.id)}
          >
            Delete folder
          </button>
        )}
      </div>

      <TaskList />

      {confirmingFolder && (
        <DeleteConfirm
          folderName={confirmingFolder.name}
          onConfirm={actions.confirmDeleteFolder}
          onCancel={actions.cancelDeleteFolder}
          returnFocusRef={deleteBtnRef}
        />
      )}
    </main>
  );
}
