import { describe, expect, it } from 'vitest';
import type { Folder, Task } from '../types';
import { initialState, reducer, type State } from './reducer';

const folder: Folder = { id: 'g', name: 'General', isDefault: true, createdAt: '', taskCount: 1 };
const task: Task = { id: '1', folderId: 'g', folderName: 'General', title: 'a', done: false, createdAt: '' };

describe('reducer', () => {
  it('loads data and becomes ready', () => {
    const s = reducer(initialState, { type: 'data/loaded', folders: [folder], tasks: [task], selection: 'g' });
    expect(s.status).toBe('ready');
    expect(s.selection).toBe('g');
    expect(s.folders).toHaveLength(1);
  });

  it('keeps the filter when selection changes', () => {
    const base: State = { ...initialState, filter: 'done', selection: 'g' };
    const s = reducer(base, { type: 'selection/set', selection: 'w' });
    expect(s.selection).toBe('w');
    expect(s.filter).toBe('done'); // filter persists across folder switches
  });

  it('closes any open delete confirmation on selection change', () => {
    const base: State = { ...initialState, confirmingDeleteFolderId: 'w' };
    const s = reducer(base, { type: 'selection/set', selection: 'g' });
    expect(s.confirmingDeleteFolderId).toBeNull();
  });

  it('upserts a task', () => {
    const base: State = { ...initialState, tasks: [task] };
    const s = reducer(base, { type: 'task/upsert', task: { ...task, done: true } });
    expect(s.tasks[0].done).toBe(true);
  });

  it('removes a task', () => {
    const base: State = { ...initialState, tasks: [task] };
    const s = reducer(base, { type: 'task/removed', id: '1' });
    expect(s.tasks).toHaveLength(0);
  });

  it('tracks folder creation form + error', () => {
    let s = reducer(initialState, { type: 'folder/startCreate' });
    expect(s.creatingFolder).toBe(true);
    s = reducer(s, { type: 'folder/error', message: 'A folder named "work" already exists.' });
    expect(s.folderError).toContain('already exists');
    s = reducer(s, { type: 'folder/cancelCreate' });
    expect(s.creatingFolder).toBe(false);
    expect(s.folderError).toBeNull();
  });
});
