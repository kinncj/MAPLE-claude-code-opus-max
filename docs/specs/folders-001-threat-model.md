# folders-001 — Threat model (STRIDE)

Scope: folders/tasks REST API and the SPA. No auth in scope (single-user, in-memory). Trust
boundary: browser → HTTP → Spring Boot → H2 (same process). The model focuses on input handling,
data integrity, and availability — the realistic risks for an unauthenticated local app.

| # | STRIDE | Threat | Surface | Mitigation | Residual |
|---|--------|--------|---------|-----------|----------|
| T1 | Tampering | Oversized/empty folder name or task title corrupts UI/state | POST /folders, /tasks | Server-side `@Valid` length (1–50 / 1–500) + trim; reject with 400 `VALIDATION`. Client mirrors but server is authoritative. | Low |
| T2 | Tampering | Duplicate folder name via race (check-then-insert) | POST /folders | Case-insensitive unique constraint at persistence; constraint violation → 409 `DUPLICATE_NAME`. | Low |
| T3 | Tampering | Orphaned tasks if folder delete partially fails | DELETE /folders/{id} | Single `@Transactional` reassign-then-delete; rollback on failure restores prior state. Invariant: task.folderId never null. | Low |
| T4 | Elevation/Logic | Deleting General removes the reassignment target → data-loss path | DELETE /folders/{id} | Domain-level `DefaultFolderProtectedException`; API returns 409; UI hides/disables the control. Enforced server-side regardless of client. | None known |
| T5 | Injection | SQL injection via name/title/id | all write paths | JPA parameterized queries / criteria; no string-concatenated SQL. UUID path params type-checked. | Low |
| T6 | Injection | Stored XSS via task/folder name rendered in SPA | task list, sidebar | React escapes text by default; no `dangerouslySetInnerHTML`. | Low |
| T7 | DoS | Unbounded task/folder creation exhausts memory | POST endpoints | In-memory store; acceptable for single-user scope. Length caps bound per-record size. Documented limitation. | Accepted |
| T8 | Info disclosure | Stack traces leak internals on error | exception handler | `@RestControllerAdvice` returns typed error bodies only; no stack traces in responses; details logged server-side. | Low |
| T9 | Repudiation | No audit of destructive ops | folder delete | Structured logs: `folder.created`, `folder.deleted`, `folder.reassigned{count}` with ids. | Accepted (no auth) |
| T10 | Tampering | Malformed UUID path param | /{id} routes | Spring converts path UUID; invalid → 400. Unknown id → 404 `NOT_FOUND`. | Low |

## Notes

- The destructive operation (folder delete) is the highest-value target; T3/T4 are the controls that
  guarantee the "never lose tasks" requirement, both enforced server-side.
- Out of scope by story: authentication, authorization, multi-tenant isolation, rate limiting.
  Adding auth later does not change the domain invariants above.
