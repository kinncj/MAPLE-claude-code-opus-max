import type { Task } from '../types';

interface Props {
  task: Task;
  showFolder: boolean;
  onToggle: (task: Task) => void;
  onDelete: (id: string) => void;
}

export function TaskRow({ task, showFolder, onToggle, onDelete }: Props) {
  return (
    <li className={`task${task.done ? ' done' : ''}`}>
      <input
        type="checkbox"
        checked={task.done}
        onChange={() => onToggle(task)}
        aria-label={`Mark "${task.title}" ${task.done ? 'not done' : 'done'}`}
      />
      <span className="task-title">{task.title}</span>
      {showFolder && <span className="folder-tag">{task.folderName}</span>}
      <button
        type="button"
        className="task-del"
        onClick={() => onDelete(task.id)}
        aria-label={`Delete "${task.title}"`}
      >
        Del
      </button>
    </li>
  );
}
