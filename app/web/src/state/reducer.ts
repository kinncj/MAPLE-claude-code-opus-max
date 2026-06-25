import type { Filter, Folder, Selection, Task } from '../types';

export interface State {
  folders: Folder[];
  tasks: Task[];
  selection: Selection;
  filter: Filter;
  status: 'loading' | 'ready' | 'error';
  error: string | null;
  confirmingDeleteFolderId: string | null;
  creatingFolder: boolean;
  folderError: string | null;
}

export const initialState: State = {
  folders: [],
  tasks: [],
  selection: '',
  filter: 'all',
  status: 'loading',
  error: null,
  confirmingDeleteFolderId: null,
  creatingFolder: false,
  folderError: null,
};

export type Action =
  | { type: 'data/loaded'; folders: Folder[]; tasks: Task[]; selection: Selection }
  | { type: 'folders/set'; folders: Folder[] }
  | { type: 'tasks/set'; tasks: Task[] }
  | { type: 'task/upsert'; task: Task }
  | { type: 'task/removed'; id: string }
  | { type: 'selection/set'; selection: Selection }
  | { type: 'filter/set'; filter: Filter }
  | { type: 'status/error'; message: string }
  | { type: 'folder/confirmDelete'; folderId: string }
  | { type: 'folder/cancelDelete' }
  | { type: 'folder/startCreate' }
  | { type: 'folder/cancelCreate' }
  | { type: 'folder/error'; message: string };

/** Pure UI-state transitions. Filter intentionally never resets on selection change (FR / UX model). */
export function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'data/loaded':
      return {
        ...state,
        folders: action.folders,
        tasks: action.tasks,
        selection: action.selection,
        status: 'ready',
        error: null,
      };
    case 'folders/set':
      return { ...state, folders: action.folders };
    case 'tasks/set':
      return { ...state, tasks: action.tasks };
    case 'task/upsert':
      return {
        ...state,
        tasks: state.tasks.map((t) => (t.id === action.task.id ? action.task : t)),
      };
    case 'task/removed':
      return { ...state, tasks: state.tasks.filter((t) => t.id !== action.id) };
    case 'selection/set':
      // Selection changes scope the list, but the completion filter persists (FR / UX model).
      return { ...state, selection: action.selection, confirmingDeleteFolderId: null };
    case 'filter/set':
      return { ...state, filter: action.filter };
    case 'status/error':
      return { ...state, status: 'error', error: action.message };
    case 'folder/confirmDelete':
      return { ...state, confirmingDeleteFolderId: action.folderId };
    case 'folder/cancelDelete':
      return { ...state, confirmingDeleteFolderId: null };
    case 'folder/startCreate':
      return { ...state, creatingFolder: true, folderError: null };
    case 'folder/cancelCreate':
      return { ...state, creatingFolder: false, folderError: null };
    case 'folder/error':
      return { ...state, folderError: action.message };
    default:
      return state;
  }
}
