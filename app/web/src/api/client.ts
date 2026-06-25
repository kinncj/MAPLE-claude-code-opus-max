import type { DeleteFolderResult, Folder, Task } from '../types';

const BASE = '/api/v1';

/** Error carrying the server's typed error code/field so the UI can show inline validation. */
export class ApiClientError extends Error {
  code: string;
  field?: string | null;
  status: number;

  constructor(status: number, code: string, message: string, field?: string | null) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.code = code;
    this.field = field;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
  if (res.status === 204) {
    return undefined as T;
  }
  const body = await res.json().catch(() => null);
  if (!res.ok) {
    const err = body?.error;
    throw new ApiClientError(
      res.status,
      err?.code ?? 'ERROR',
      err?.message ?? `Request failed (${res.status})`,
      err?.field,
    );
  }
  return body as T;
}

export const api = {
  listFolders: () => request<Folder[]>('/folders'),

  createFolder: (name: string) =>
    request<Folder>('/folders', { method: 'POST', body: JSON.stringify({ name }) }),

  deleteFolder: (id: string) =>
    request<DeleteFolderResult>(`/folders/${id}`, { method: 'DELETE' }),

  listTasks: (folderId?: string) =>
    request<Task[]>(folderId ? `/tasks?folderId=${encodeURIComponent(folderId)}` : '/tasks'),

  createTask: (folderId: string, title: string) =>
    request<Task>('/tasks', { method: 'POST', body: JSON.stringify({ folderId, title }) }),

  updateTask: (id: string, patch: { done?: boolean; title?: string }) =>
    request<Task>(`/tasks/${id}`, { method: 'PATCH', body: JSON.stringify(patch) }),

  deleteTask: (id: string) => request<void>(`/tasks/${id}`, { method: 'DELETE' }),
};
