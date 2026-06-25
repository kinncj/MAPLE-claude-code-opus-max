import { beforeEach, describe, expect, it, vi, type Mock } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { Folder, Task } from './types';

const general: Folder = { id: 'g', name: 'General', isDefault: true, createdAt: '2026-01-01', taskCount: 2 };
const work: Folder = { id: 'w', name: 'Work', isDefault: false, createdAt: '2026-01-02', taskCount: 1 };

const generalTasks: Task[] = [
  { id: 't1', folderId: 'g', folderName: 'General', title: 'Buy milk', done: false, createdAt: '2026-01-01' },
  { id: 't2', folderId: 'g', folderName: 'General', title: 'Walk dog', done: true, createdAt: '2026-01-01' },
];
const allTasks: Task[] = [
  ...generalTasks,
  { id: 't3', folderId: 'w', folderName: 'Work', title: 'Prepare deck', done: false, createdAt: '2026-01-02' },
];

vi.mock('./api/client', () => ({
  ApiClientError: class extends Error {},
  api: {
    listFolders: vi.fn(() => Promise.resolve([general, work])),
    listTasks: vi.fn((folderId?: string) => Promise.resolve(folderId ? generalTasks : allTasks)),
    createFolder: vi.fn(),
    deleteFolder: vi.fn(),
    createTask: vi.fn(),
    updateTask: vi.fn(),
    deleteTask: vi.fn(),
  },
}));

import { App } from './App';
import { api } from './api/client';

beforeEach(() => {
  localStorage.clear();
  document.documentElement.removeAttribute('data-theme');
});

describe('App', () => {
  it('loads folders, stars the active one, and scopes the add-task placeholder', async () => {
    render(<App />);
    const generalBtn = await screen.findByRole('button', { name: /General, 2 tasks, active folder/ });
    expect(generalBtn).toHaveAttribute('aria-current', 'true');
    expect(within(generalBtn).getByText('★')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Add new task in General ...')).toBeInTheDocument();
    expect(await screen.findByText('Buy milk')).toBeInTheDocument();
  });

  it('toggles and persists the theme', async () => {
    render(<App />);
    await screen.findByText('Buy milk');
    const toggle = screen.getByRole('button', { name: 'Toggle dark theme' });
    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
    await userEvent.click(toggle);
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    expect(localStorage.getItem('todo.theme')).toBe('dark');
  });

  it('shows folder labels in the All-folders view and hides delete-folder', async () => {
    render(<App />);
    await screen.findByText('Buy milk');
    await userEvent.click(screen.getByRole('button', { name: '▼ All folders' }));
    const row = (await screen.findByText('Prepare deck')).closest('li') as HTMLElement;
    expect(within(row).getByText('Work')).toBeInTheDocument(); // folder tag on the task row
    expect(screen.queryByRole('button', { name: /Delete folder/ })).not.toBeInTheDocument();
  });

  it('hides the delete-folder action for General', async () => {
    render(<App />);
    await screen.findByText('Buy milk');
    expect(screen.queryByRole('button', { name: /Delete folder/ })).not.toBeInTheDocument();
  });

  it('reverts an optimistic toggle when the server rejects', async () => {
    (api.updateTask as Mock).mockRejectedValueOnce(new Error('network'));
    render(<App />);
    const checkbox = await screen.findByRole('checkbox', { name: 'Mark "Buy milk" done' });
    expect(checkbox).not.toBeChecked();
    await userEvent.click(checkbox);
    // After the rejected update, the provider re-syncs from the server → back to not done.
    await waitFor(() =>
      expect(screen.getByRole('checkbox', { name: 'Mark "Buy milk" done' })).not.toBeChecked(),
    );
  });
});
