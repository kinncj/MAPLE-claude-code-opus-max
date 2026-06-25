import { ALL_FOLDERS } from '../types';
import { useTodo } from '../state/TodoProvider';
import { activeFolder, filterTasks } from '../state/selectors';
import { TaskRow } from './TaskRow';

export function TaskList() {
  const { state, actions } = useTodo();
  const { tasks, filter, selection, folders } = state;

  const isAll = selection === ALL_FOLDERS;
  const visible = filterTasks(tasks, filter);
  const active = activeFolder(folders, selection);

  if (tasks.length === 0) {
    const message = isAll
      ? 'No tasks yet. Create a folder or add a task to get started.'
      : `No tasks in "${active?.name ?? 'this folder'}" yet.`;
    return (
      <div className="task-panel">
        <p className="empty-state">{message}</p>
      </div>
    );
  }

  if (visible.length === 0) {
    const message = filter === 'done' ? 'No completed tasks.' : 'No tasks to do — all done!';
    return (
      <div className="task-panel">
        <p className="empty-state">{message}</p>
      </div>
    );
  }

  return (
    <div className="task-panel">
      <ul className="task-list" aria-label="Tasks">
        {visible.map((t) => (
          <TaskRow
            key={t.id}
            task={t}
            showFolder={isAll}
            onToggle={actions.toggleTask}
            onDelete={actions.deleteTask}
          />
        ))}
      </ul>
    </div>
  );
}
