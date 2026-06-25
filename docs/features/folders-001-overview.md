# Feature: Folder-based organization for the TODO SPA

Story: `folders-001` · Issue: #1 · Status: implemented.

## What it does

Tasks belong to exactly one folder. Users can:

- Create folders and switch between them. Selecting a folder scopes the task list and the
  add-task input to that folder.
- Filter the visible list by `All / Todo / Done`. The filter persists across folder switches.
- Delete a folder. Its tasks reassign to the default `General` folder in one transaction, then the
  folder is removed. `General` cannot be deleted.
- See a live task count on every folder. The active folder is starred and highlighted.
- Toggle a persisted light/dark theme that also respects the OS preference on first load.
- View `All folders`, an aggregate of every task, labeled by folder, with the delete action hidden.

## Architecture

Hexagonal layering (see [ADR-0001](../specs/adrs/0001-folder-task-domain-and-reassignment.md)
and the [architecture diagrams](../architecture/folders-001-architecture.md)).

| Layer | Location | Responsibility |
|-------|----------|----------------|
| Domain | `app/api/.../domain` | `Folder`, `Task`, ports. Pure Java, zero framework imports. |
| Application | `app/api/.../application` | `FolderService`, `TaskService`. Framework-free; wired via `@Configuration`. |
| Infrastructure | `app/api/.../infrastructure` | JPA adapters (H2), the transactional reassign-then-delete, structured audit logging. |
| Web | `app/api/.../web` | REST controllers, DTOs, exception handler. |
| Frontend | `app/web/src` | Vite + React + TS SPA. `useReducer` store, token-driven CSS, WCAG 2.2 AA. |

The SPA is served as static resources by Spring Boot, so browser, API, and store share one origin
(`:8080`): no CORS, no proxy. Persistence is H2 in-memory.

## Key guarantees

- **No data loss on delete.** Reassignment and folder removal are a single `@Transactional`
  operation; the reassignment target (`General`) is resolved inside that transaction.
- **Exactly one default.** A `unique` `default_marker` column enforces a single `General` at the
  database level, independent of application code.
- **Case-insensitive unique names.** Backed by a `name_ci` unique column; empty/whitespace rejected.
- **Counts are authoritative.** A single grouped query feeds per-folder counts, avoiding N+1.

## Running it

```bash
make run          # build + run at http://localhost:8080 (Docker)
make test-all     # unit + integration + e2e + contract (Phase 8 gate)
make test         # backend + frontend unit tests
make test-e2e     # Playwright against the running container
```

- App: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI: <http://localhost:8080/v3/api-docs>
- Health: <http://localhost:8080/actuator/health>

Tooling that may be absent locally (Maven, Playwright browsers) runs via Docker.

## Tests

| Layer | Count | Location |
|-------|-------|----------|
| Domain + application unit | 30 | `app/api/src/test/.../domain`, `.../application` |
| API integration (H2) | 17 | `app/api/src/test/.../web/*IT.java` |
| Frontend unit | 31 | `app/web/src/**/*.test.tsx` |
| E2E (all 13 Gherkin scenarios) | 13 | `tests/e2e/folders.spec.ts` |
| Accessibility (axe, both themes) | 0 violations | `docs/design/mockups/folders-001.a11y.json` |

## Observability

Structured logs on the `todo.audit` logger: `event=folder.created`, `event=folder.reassigned`
(with `reassignedCount`), `event=folder.deleted`.

## Scope

Non-goals (not built): auth/multi-user, nested folders, drag-and-drop, task metadata beyond
title + done, folder sharing/search/bulk operations. See the [story](../stories/folders-001-folder-organization.md).

## Known limitations / fast-follow

Concurrency is out of scope for this single-user in-memory build. Two items become relevant only if
this moves to a real multi-writer database, and are deferred:

- `SeedService.ensureDefault()` uses check-then-save. The `default_marker` unique constraint already
  prevents a second default, but the losing writer would surface a raw integrity exception on boot
  rather than converging gracefully. Hardening: catch and re-read in a fresh transaction.
- The reset endpoint (`/api/v1/test/reset`) is enabled by default for the demo. Disable
  (`todo.test-support=false`) in any non-demo deployment.
