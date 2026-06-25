# API contract — TODO Folders (v1)

Base path: `/api/v1`. Source of truth: [`folders-001.openapi.yaml`](../specs/current/contracts/folders-001.openapi.yaml).
Live docs: Swagger UI at `/swagger-ui.html`, spec at `/v3/api-docs`.

## Folders

| Method | Path | Body | Success | Errors |
|--------|------|------|---------|--------|
| GET | `/folders` | — | 200 `Folder[]` (General first) | — |
| POST | `/folders` | `{ "name": string }` | 201 `Folder` | 400 `VALIDATION`, 409 `DUPLICATE_NAME` |
| DELETE | `/folders/{id}` | — | 200 `DeleteFolderResult` | 404 `NOT_FOUND`, 409 `CANNOT_DELETE_DEFAULT` |

## Tasks

| Method | Path | Body | Success | Errors |
|--------|------|------|---------|--------|
| GET | `/tasks?folderId={id}` | — | 200 `Task[]` (omit `folderId` for all folders) | 404 `NOT_FOUND` |
| POST | `/tasks` | `{ "folderId": uuid, "title": string }` | 201 `Task` | 400 `VALIDATION`, 404 `NOT_FOUND` |
| PATCH | `/tasks/{id}` | `{ "done"?: boolean, "title"?: string }` | 200 `Task` | 400 `VALIDATION`, 404 `NOT_FOUND` |
| DELETE | `/tasks/{id}` | — | 204 | 404 `NOT_FOUND` |

## Schemas

```jsonc
// Folder
{ "id": "uuid", "name": "Work", "isDefault": false, "createdAt": "2026-06-25T03:56:21Z", "taskCount": 1 }

// Task
{ "id": "uuid", "folderId": "uuid", "folderName": "Work", "title": "Ship release", "done": false, "createdAt": "..." }

// DeleteFolderResult
{ "deletedFolderId": "uuid", "reassignedCount": 1, "generalFolderId": "uuid" }

// Error envelope (all 4xx)
{ "error": { "code": "DUPLICATE_NAME", "message": "A folder named \"work\" already exists.", "field": "name" } }
```

## Validation rules

- Folder `name`: trimmed, 1–50 chars, unique case-insensitive. Empty/whitespace → 400 `VALIDATION`;
  duplicate → 409 `DUPLICATE_NAME`.
- Task `title`: trimmed, 1–500 chars. `folderId` required and must reference an existing folder.
- Deleting `General` → 409 `CANNOT_DELETE_DEFAULT`. Deleting any other folder reassigns its tasks to
  `General` and reports `reassignedCount`.

## Error codes

| Code | HTTP | Meaning |
|------|------|---------|
| `VALIDATION` | 400 | Missing/invalid field (e.g. empty name, missing folderId). |
| `DUPLICATE_NAME` | 409 | A folder with that name (case-insensitive) already exists. |
| `NOT_FOUND` | 404 | Folder or task id does not exist. |
| `CANNOT_DELETE_DEFAULT` | 409 | Attempt to delete the protected `General` folder. |

## Test support (non-production)

`POST /api/v1/test/reset` resets the in-memory store to the seeded Background (General=3, Work=1,
Groceries=2). Enabled only when `todo.test-support=true`. Used by the e2e suite for determinism.
