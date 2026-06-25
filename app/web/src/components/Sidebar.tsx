import { ALL_FOLDERS } from '../types';
import { useTodo } from '../state/TodoProvider';
import { NewFolderForm } from './NewFolderForm';

export function Sidebar() {
  const { state, actions } = useTodo();
  const { folders, selection, creatingFolder, folderError } = state;

  return (
    <aside className="sidebar" aria-label="Folders">
      <h2 className="sidebar-title">Folders</h2>

      <ul className="folder-list">
        {folders.map((f) => {
          const active = selection === f.id;
          return (
            <li key={f.id}>
              <button
                type="button"
                className={`folder${active ? ' active' : ''}`}
                aria-current={active ? 'true' : undefined}
                aria-label={`${f.name}, ${f.taskCount} ${f.taskCount === 1 ? 'task' : 'tasks'}${active ? ', active folder' : ''}`}
                onClick={() => actions.selectFolder(f.id)}
              >
                <span className="folder-name">
                  {active && <span className="star" aria-hidden="true">★</span>}
                  {f.name}
                </span>
                <span className="folder-count" aria-hidden="true">{f.taskCount}</span>
              </button>
            </li>
          );
        })}
      </ul>

      <div className="sidebar-spacer" />

      {creatingFolder ? (
        <NewFolderForm
          error={folderError}
          onSubmit={actions.submitCreateFolder}
          onCancel={actions.cancelCreateFolder}
        />
      ) : (
        <button type="button" className="side-btn" onClick={actions.startCreateFolder}>
          + New folder
        </button>
      )}

      <button
        type="button"
        className={`side-btn${selection === ALL_FOLDERS ? ' active' : ''}`}
        aria-pressed={selection === ALL_FOLDERS}
        onClick={() => actions.selectFolder(ALL_FOLDERS)}
      >
        ▼ All folders
      </button>
    </aside>
  );
}
