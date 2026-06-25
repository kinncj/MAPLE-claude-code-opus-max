# ADR-0001 — Folder/Task domain model and transactional reassignment-on-delete

- Status: Accepted
- Date: 2026-06-24
- Story: folders-001
- Deciders: architect, orchestrator

## 1. Title

Folder/Task domain model, persistence boundary, and transactional reassignment-on-delete.

## 2. Context

The TODO SPA gains folders as a first-class grouping dimension. Tasks belong to exactly
one folder. Deleting a folder must never lose tasks: its tasks reassign to the default
`General` folder. `General` is always present and cannot be deleted. The backend is Java 21 +
Spring Boot 3.3.x with an in-memory (H2) store. Constraints require Clean Architecture (domain
logic free of framework/infrastructure imports), SOLID, a versioned REST API, and a single
transactional reassign-then-delete operation.

## 3. Goals / Non-goals

**Goals**
- Domain layer (`Folder`, `Task`, services) with zero Spring/JPA imports.
- Reassign-on-delete is atomic: tasks move to General, then the folder is removed, in one transaction.
- `General` protected from deletion at the domain layer, not just the UI.
- Live per-folder counts without N+1 queries.

**Non-goals**
- Auth/multi-user, nested folders, task metadata beyond title+done, moving tasks via DnD.

## 4. Proposal

Hexagonal layering inside `app/api`:

- **domain.model** — `Folder`, `Task` as pure classes. Invariants enforced in static factories
  (`Folder.create`: name trimmed, 1–50 chars; `Task.create`: title trimmed, 1–500) and behavior
  methods (`markDone`, `reassignTo`). No annotations.
- **domain.repository** — ports: `FolderRepository`, `TaskRepository`. Pure interfaces.
- **application.service** — `FolderService`, `TaskService`. Constructor-inject ports. Framework-free;
  registered as beans via an explicit `@Configuration` (no `@Service` on the classes) so the
  application layer stays import-clean. Enforce duplicate-name (case-insensitive), General-protection,
  and orchestrate reassignment.
- **infrastructure.persistence** — JPA entities (`FolderJpaEntity`, `TaskJpaEntity`), Spring Data
  repositories, and adapters implementing the domain ports. The atomic
  `deleteFolderAndReassign(folderId, generalId)` lives on the adapter and is annotated
  `@Transactional` — the only place a transaction boundary is declared.
- **web** — `FolderController`, `TaskController`, request/response DTO records, and a
  `@RestControllerAdvice` mapping domain exceptions to RFC-style error bodies.
- **config** — `DataInitializer` (`ApplicationRunner`) creates `General` idempotently on boot;
  bean wiring; structured logging of folder create/delete and reassignment counts.

Counts are computed with a single `GROUP BY folderId` aggregate, assembled into folder DTOs —
no per-folder count query.

## 5. Alternatives

1. **JPA annotations on domain entities** — fewer classes, but couples domain to the ORM and
   violates the zero-framework-import constraint. Rejected.
2. **Service-level `@Transactional`** — would require the application layer to import Spring.
   Rejected in favor of a transactional adapter method behind the port.
3. **`ON DELETE SET DEFAULT` at the DB level** — not portable, hides business logic in DDL, and
   loses the reassignment count needed for observability. Rejected; reassignment is application-enforced.

## 6. Trade-offs and Risks

- **More classes** (separate domain vs JPA entities + mappers). Accepted: the boundary is the point.
- **In-memory store** resets on restart. Acceptable per story (no persistence requirement); the port
  abstraction lets a durable adapter replace H2 with no domain change.
- **Race on duplicate name**: check-then-insert is not atomic under concurrency. Mitigated by a
  case-insensitive unique constraint at the persistence layer; the service maps the constraint
  violation to `DUPLICATE_NAME`.

## 7. Impact

- **FinOps** — single process, in-memory DB; negligible cost. No managed database.
- **SRE** — failure modes: reassignment partial failure (mitigated by single transaction; rollback
  restores prior state). Blast radius: one process. Observability: structured logs emit
  `folder.created`, `folder.deleted`, and `folder.reassigned{count}`.
- **Security** — no auth in scope; input validation guards length/format; no PII.
- **Team** — clear layer ownership; ports enable parallel work and test doubles.

## 8. Decision

Adopt the hexagonal layering above with a transactional reassign-then-delete adapter method and
domain-level General-protection. The REST contract is fixed in
`docs/specs/current/contracts/folders-001.openapi.yaml`.

## 9. Next Steps

- Implement domain, ports, services, JPA adapters, controllers, exception handler, bootstrap.
- Domain unit tests (rules, General-protection, reassignment), API integration tests (H2), e2e.
- Wire Makefile + Docker so `make run` and `make test-all` execute the contract end-to-end.
