export interface Folder {
  id: string;
  name: string;
  isDefault: boolean;
  createdAt: string;
  taskCount: number;
}

export interface Task {
  id: string;
  folderId: string;
  folderName: string;
  title: string;
  done: boolean;
  createdAt: string;
}

export interface DeleteFolderResult {
  deletedFolderId: string;
  reassignedCount: number;
  generalFolderId: string;
}

export interface ApiError {
  error: { code: string; message: string; field?: string | null };
}

export type Filter = 'all' | 'todo' | 'done';

/** Sentinel for the cross-folder "All folders" view. */
export const ALL_FOLDERS = 'ALL' as const;
export type Selection = string | typeof ALL_FOLDERS;
