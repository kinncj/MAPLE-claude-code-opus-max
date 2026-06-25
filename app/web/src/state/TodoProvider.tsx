import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useReducer,
  useRef,
  type ReactNode,
} from 'react';
import { api, ApiClientError } from '../api/client';
import { ALL_FOLDERS, type Filter, type Selection, type Task } from '../types';
import { initialState, reducer, type State } from './reducer';
import { defaultFolder, targetFolderForAdd } from './selectors';

export interface TodoActions {
  selectFolder: (selection: Selection) => Promise<void>;
  setFilter: (filter: Filter) => void;
  addTask: (title: string) => Promise<void>;
  toggleTask: (task: Task) => Promise<void>;
  deleteTask: (id: string) => Promise<void>;
  requestDeleteFolder: (folderId: string) => void;
  cancelDeleteFolder: () => void;
  confirmDeleteFolder: () => Promise<void>;
  startCreateFolder: () => void;
  cancelCreateFolder: () => void;
  submitCreateFolder: (name: string) => Promise<boolean>;
}

interface TodoContextValue {
  state: State;
  actions: TodoActions;
}

const TodoContext = createContext<TodoContextValue | null>(null);

function errorMessage(e: unknown): string {
  return e instanceof Error ? e.message : 'Something went wrong';
}

export function TodoProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(reducer, initialState);
  const stateRef = useRef(state);
  stateRef.current = state;

  // Monotonic token so a slow, out-of-order task response can't overwrite a newer one.
  const reqSeq = useRef(0);

  const tasksFor = (selection: Selection) =>
    api.listTasks(selection === ALL_FOLDERS ? undefined : selection);

  const reload = useCallback(async (selection: Selection) => {
    const seq = ++reqSeq.current;
    const [folders, tasks] = await Promise.all([api.listFolders(), tasksFor(selection)]);
    if (seq !== reqSeq.current) {
      return;
    }
    dispatch({ type: 'folders/set', folders });
    dispatch({ type: 'tasks/set', tasks });
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const folders = await api.listFolders();
        const selection: Selection = defaultFolder(folders)?.id ?? folders[0]?.id ?? '';
        const tasks = await tasksFor(selection);
        dispatch({ type: 'data/loaded', folders, tasks, selection });
      } catch (e) {
        dispatch({ type: 'status/error', message: errorMessage(e) });
      }
    })();
  }, []);

  const selectFolder = useCallback(async (selection: Selection) => {
    const seq = ++reqSeq.current;
    dispatch({ type: 'selection/set', selection });
    try {
      const tasks = await tasksFor(selection);
      if (seq === reqSeq.current) {
        dispatch({ type: 'tasks/set', tasks });
      }
    } catch (e) {
      if (seq === reqSeq.current) {
        dispatch({ type: 'status/error', message: errorMessage(e) });
      }
    }
  }, []);

  const setFilter = useCallback((filter: Filter) => {
    dispatch({ type: 'filter/set', filter });
  }, []);

  const addTask = useCallback(async (title: string) => {
    const s = stateRef.current;
    const target = targetFolderForAdd(s.folders, s.selection);
    if (!target || !title.trim()) {
      return;
    }
    await api.createTask(target.id, title.trim());
    await reload(s.selection);
  }, [reload]);

  const toggleTask = useCallback(async (task: Task) => {
    // Optimistic: reflect the toggle immediately, reconcile or resync from the server on failure.
    const optimistic = { ...task, done: !task.done };
    dispatch({ type: 'task/upsert', task: optimistic });
    try {
      const updated = await api.updateTask(task.id, { done: optimistic.done });
      dispatch({ type: 'task/upsert', task: updated });
    } catch {
      // Re-sync from the server rather than reverting to a possibly stale snapshot.
      await reload(stateRef.current.selection);
    }
  }, [reload]);

  const deleteTask = useCallback(async (id: string) => {
    await api.deleteTask(id);
    dispatch({ type: 'task/removed', id });
    // Refresh counts via the seq-guarded reload so a stale response can't overwrite a newer one.
    await reload(stateRef.current.selection);
  }, [reload]);

  const requestDeleteFolder = useCallback((folderId: string) => {
    dispatch({ type: 'folder/confirmDelete', folderId });
  }, []);

  const cancelDeleteFolder = useCallback(() => {
    dispatch({ type: 'folder/cancelDelete' });
  }, []);

  const confirmDeleteFolder = useCallback(async () => {
    const id = stateRef.current.confirmingDeleteFolderId;
    if (!id) {
      return;
    }
    const result = await api.deleteFolder(id);
    dispatch({ type: 'folder/cancelDelete' });
    dispatch({ type: 'selection/set', selection: result.generalFolderId });
    await reload(result.generalFolderId);
  }, [reload]);

  const startCreateFolder = useCallback(() => dispatch({ type: 'folder/startCreate' }), []);
  const cancelCreateFolder = useCallback(() => dispatch({ type: 'folder/cancelCreate' }), []);

  const submitCreateFolder = useCallback(async (name: string): Promise<boolean> => {
    try {
      const folder = await api.createFolder(name);
      dispatch({ type: 'folder/cancelCreate' });
      dispatch({ type: 'selection/set', selection: folder.id });
      await reload(folder.id);
      return true;
    } catch (e) {
      const message = e instanceof ApiClientError ? e.message : 'Could not create folder';
      dispatch({ type: 'folder/error', message });
      return false;
    }
  }, [reload]);

  const actions: TodoActions = {
    selectFolder,
    setFilter,
    addTask,
    toggleTask,
    deleteTask,
    requestDeleteFolder,
    cancelDeleteFolder,
    confirmDeleteFolder,
    startCreateFolder,
    cancelCreateFolder,
    submitCreateFolder,
  };

  return <TodoContext.Provider value={{ state, actions }}>{children}</TodoContext.Provider>;
}

export function useTodo(): TodoContextValue {
  const ctx = useContext(TodoContext);
  if (!ctx) {
    throw new Error('useTodo must be used within a TodoProvider');
  }
  return ctx;
}
