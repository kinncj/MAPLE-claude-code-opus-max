# Changelog

All notable changes to this project are documented here. Format loosely follows
[Keep a Changelog](https://keepachangelog.com/); this project uses Conventional Commits.

## [Unreleased]

### Added — folder-based organization (folders-001, #1)

- **Backend** (Java 21, Spring Boot 3.3, H2 in-memory): versioned REST API at `/api/v1` for folders
  and tasks. Clean Architecture — a framework-free domain and application core behind ports, with
  JPA adapters in the infrastructure layer.
  - Folder delete reassigns the folder's tasks to the default `General` folder and removes the
    folder in a single transaction; the reassignment target is resolved inside that transaction.
  - `General` cannot be deleted (enforced in the domain) and is guaranteed unique by a database
    constraint.
  - Case-insensitive unique folder names; trimmed length validation for names (1–50) and task
    titles (1–500); typed error envelope.
  - Per-folder task counts via a single grouped query.
  - Swagger UI (`/swagger-ui.html`), OpenAPI (`/v3/api-docs`), and actuator health.
  - Structured audit logs for folder create / reassign / delete.
- **Frontend** (Vite + React + TypeScript): folders sidebar with live counts and a starred active
  folder; folder-scoped task list and add-task input; `All / Todo / Done` filter that persists
  across folder switches; inline, focus-managed delete confirmation; `All folders` aggregate view;
  persisted light/dark theme that respects the OS preference on first load. Token-driven styling;
  WCAG 2.2 AA in both themes.
- **Infra**: multi-stage Docker build (vite → maven → JRE) serving the SPA and API from one origin;
  `make run`, `make test`, `make test-integration`, `make test-e2e`, `make test-contract`,
  `make test-all`.
- **Docs**: ADR-0001, OpenAPI contract, STRIDE threat model, architecture diagrams, feature
  overview, and API reference.

### Tests

- 30 backend unit, 17 API integration (H2), 31 frontend unit, 13 end-to-end (every Gherkin
  scenario). Accessibility audit (axe) clean in light and dark themes.
