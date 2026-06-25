import { useState } from 'react';
import { useTodo } from '../state/TodoProvider';
import { addPlaceholder } from '../state/selectors';

export function AddTask() {
  const { state, actions } = useTodo();
  const [value, setValue] = useState('');
  const placeholder = addPlaceholder(state.folders, state.selection);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!value.trim()) {
      return;
    }
    await actions.addTask(value);
    setValue('');
  };

  return (
    <form className="add-task" onSubmit={submit}>
      <input
        type="text"
        className="add-task-input"
        value={value}
        placeholder={placeholder}
        aria-label={placeholder.replace(/\s*\.\.\.$/, '')}
        onChange={(e) => setValue(e.target.value)}
      />
      <button type="submit" className="add-task-btn" aria-label="Add task">
        <span aria-hidden="true">+</span>
      </button>
    </form>
  );
}
