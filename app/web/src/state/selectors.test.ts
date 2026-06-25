import { describe, expect, it } from 'vitest';
import { ALL_FOLDERS, type Folder, type Task } from '../types';
import { activeFolder, addPlaceholder, filterTasks, targetFolderForAdd } from './selectors';

const folders: Folder[] = [
  { id: 'g', name: 'General', isDefault: true, createdAt: '', taskCount: 3 },
  { id: 'w', name: 'Work', isDefault: false, createdAt: '', taskCount: 1 },
];

const tasks: Task[] = [
  { id: '1', folderId: 'g', folderName: 'General', title: 'a', done: false, createdAt: '' },
  { id: '2', folderId: 'g', folderName: 'General', title: 'b', done: true, createdAt: '' },
];

describe('filterTasks', () => {
  it('returns all for "all"', () => {
    expect(filterTasks(tasks, 'all')).toHaveLength(2);
  });
  it('returns only not-done for "todo"', () => {
    expect(filterTasks(tasks, 'todo').map((t) => t.id)).toEqual(['1']);
  });
  it('returns only done for "done"', () => {
    expect(filterTasks(tasks, 'done').map((t) => t.id)).toEqual(['2']);
  });
});

describe('activeFolder', () => {
  it('finds the selected folder', () => {
    expect(activeFolder(folders, 'w')?.name).toBe('Work');
  });
  it('is null in the All-folders view', () => {
    expect(activeFolder(folders, ALL_FOLDERS)).toBeNull();
  });
});

describe('targetFolderForAdd', () => {
  it('uses the active folder', () => {
    expect(targetFolderForAdd(folders, 'w')?.name).toBe('Work');
  });
  it('falls back to General in the All-folders view', () => {
    expect(targetFolderForAdd(folders, ALL_FOLDERS)?.name).toBe('General');
  });
});

describe('addPlaceholder', () => {
  it('reflects the active folder', () => {
    expect(addPlaceholder(folders, 'w')).toBe('Add new task in Work ...');
  });
  it('reflects General in the All view', () => {
    expect(addPlaceholder(folders, ALL_FOLDERS)).toBe('Add new task in General ...');
  });
});
