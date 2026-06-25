import { ALL_FOLDERS, type Filter, type Folder, type Selection, type Task } from '../types';

/** Apply the completion-state filter to an already folder-scoped task list. */
export function filterTasks(tasks: Task[], filter: Filter): Task[] {
  switch (filter) {
    case 'todo':
      return tasks.filter((t) => !t.done);
    case 'done':
      return tasks.filter((t) => t.done);
    case 'all':
    default:
      return tasks;
  }
}

/** The active folder, or null in the All-folders view. */
export function activeFolder(folders: Folder[], selection: Selection): Folder | null {
  if (selection === ALL_FOLDERS) {
    return null;
  }
  return folders.find((f) => f.id === selection) ?? null;
}

export function defaultFolder(folders: Folder[]): Folder | undefined {
  return folders.find((f) => f.isDefault);
}

/**
 * Where a new task is created from the current selection:
 * the active folder, or General in the All-folders view (FR-11).
 */
export function targetFolderForAdd(folders: Folder[], selection: Selection): Folder | undefined {
  if (selection === ALL_FOLDERS) {
    return defaultFolder(folders);
  }
  return folders.find((f) => f.id === selection) ?? defaultFolder(folders);
}

/** Placeholder text for the add-task input (FR-7 / FR-11). */
export function addPlaceholder(folders: Folder[], selection: Selection): string {
  const target = targetFolderForAdd(folders, selection);
  return `Add new task in ${target?.name ?? 'General'} ...`;
}
