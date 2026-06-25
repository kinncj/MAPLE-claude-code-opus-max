---
id: "folders-001"
title: "Folder-based organization for the TODO SPA"
epic: "todo-organization"
priority: "high"
ui: true
adr_required: true
milestone: null
phase: discover
labels:
  - "type:feature"
  - "priority:high"
  - "area:frontend"
  - "area:backend"
  - "ui"
status: draft
issue_number: 1
---

## Story

**As a** person managing many tasks,
**I want** to group my tasks into named folders, scope the list and the add-task input to one folder, and safely delete folders without losing tasks,
**so that** I can keep unrelated work separated while never risking data loss.

## Context

- Target: `todo` BusinessRepo. Frontend: Vite + React + TypeScript. Backend: Java 21 + Spring Boot 3.3.x. Persistence: in-memory (H2).
- Folders become a first-class grouping dimension over a previously flat task list.
- Source of truth for UI/UX is the wireframe at `docs/design/wireframes/folders-001.wireframe.*`.

## Goals

- Group tasks under user-defined folders.
- Scope task operations (list, add, filter) to the selected folder.
- Safe folder deletion with task reassignment to `General`, never data loss.
- Live per-folder task counts.
- Persisted light/dark theme.

## Non-goals

- Auth / multi-user / per-user partitioning.
- Nested/hierarchical folders.
- Drag-and-drop reordering or moving tasks between folders via DnD.
- Task metadata beyond `title` + `done` (no due dates, priorities, tags).
- Folder sharing, search, or bulk operations.

## Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-1  | Sidebar lists all folders with a task-count badge: `name (n)`. |
| FR-2  | Exactly one folder is active; active folder is highlighted and prefixed with `★`. |
| FR-3  | Selecting a folder scopes the task list and the add-task input to that folder. |
| FR-4  | `General` is the default folder, always present, and cannot be deleted. |
| FR-5  | `+ New folder` creates a folder after name entry; the new folder becomes active. |
| FR-6  | Delete-folder triggers an inline confirmation; on confirm, the folder's tasks reassign to `General` and `General` becomes active. |
| FR-7  | Add-task placeholder reflects the active folder: `Add new task in <folder> ...`. `[+]` adds the task to the active folder. |
| FR-8  | Each task row: checkbox (done toggle), title, `[Del]` to delete the task. |
| FR-9  | Filter control `All / Todo / Done` filters the visible list by completion state. |
| FR-10 | Theme toggle switches light/dark; choice persists across reloads. |
| FR-11 | `All folders` shows tasks across every folder, labeled by folder; add-task defaults to `General`; the delete-folder action is hidden in this view. |

## Data Model

**Folder** — `id: UUID (PK)`, `name: string (trimmed, 1–50, case-insensitive unique)`, `isDefault: boolean (true only for General)`, `createdAt: timestamp`. Derived `taskCount = count(tasks where folderId = id)`.

**Task** — `id: UUID (PK)`, `folderId: UUID (FK→Folder, not null)`, `title: string (trimmed, 1–500)`, `done: boolean (default false)`, `createdAt: timestamp`.

**Invariants** — every task has exactly one folder; `General` is created idempotently on first boot; folder delete reassigns tasks transactionally before removal.

## Acceptance Criteria

```gherkin
@story:folders-001 @epic:todo-organization @priority:high @ui
Feature: Folder-based task organization

  Background:
    Given the default folder "General" exists
    And folder "Work" exists with 1 task
    And folder "Groceries" exists with 2 tasks

  Scenario: Active folder is highlighted with count
    When I open the app
    Then "General" is the active folder
    And each folder shows its task count
    And the active folder is starred

  Scenario: Selecting a folder scopes the list and input
    When I select folder "Work"
    Then the task list shows only "Work" tasks
    And the add-task placeholder reads 'Add new task in Work ...'

  Scenario: Add a task to the active folder
    Given folder "Work" is active
    When I enter "Ship release" and confirm add
    Then "Ship release" appears in the "Work" task list
    And the "Work" count increases by 1

  Scenario: Toggle task completion
    Given a task "Buy milk" exists and is not done
    When I toggle its checkbox
    Then "Buy milk" is marked done

  Scenario: Delete a task
    Given a task "Walk dog" exists in the active folder
    When I delete "Walk dog"
    Then it no longer appears
    And the active folder count decreases by 1

  Scenario: Filter by completion state
    Given the active folder has done and not-done tasks
    When I select the "Done" filter
    Then only done tasks are shown
    When I select the "Todo" filter
    Then only not-done tasks are shown

  Scenario: Delete a folder reassigns tasks to General
    Given folder "Work" has 1 task and is active
    When I choose to delete folder "Work"
    Then I see "Delete folder \"Work\"? Tasks move to General."
    When I confirm
    Then folder "Work" no longer exists
    And its task now belongs to "General"
    And "General" is the active folder
    And the "General" count increased by 1

  Scenario: Cancel folder deletion
    When I choose to delete folder "Work"
    And I cancel
    Then folder "Work" still exists with its tasks unchanged

  Scenario: General cannot be deleted
    When "General" is the active folder
    Then the delete-folder action is unavailable for "General"

  Scenario: Create a new folder
    When I create a folder named "Reading"
    Then "Reading" appears in the sidebar with count 0
    And "Reading" becomes the active folder

  Scenario: Reject invalid folder names
    When I try to create a folder with an empty name
    Then creation is rejected with an inline validation message
    When I try to create a folder named "work"
    Then creation is rejected as a duplicate

  Scenario: Theme toggle persists
    When I switch to dark theme
    And I reload the app
    Then dark theme is still applied

  Scenario: View all folders
    When I select "All folders"
    Then tasks from every folder are shown, labeled by folder
    And the delete-folder action is hidden
```

## State / UX Model

- **Active selection:** a single folder id, or the `ALL` pseudo-folder.
- **Filter:** `All | Todo | Done`; persists across folder switches within a session (does not reset on folder change). Default `All`.
- **Theme:** `light | dark`, persisted in `localStorage`, respects `prefers-color-scheme` on first load.
- **Confirmation:** inline, non-blocking, scoped to the right panel. `[Cancel]` is the default/safe action; `Esc` cancels.

### Edge cases (must handle)

- Delete the active folder → reassign tasks to `General`, set `General` active.
- Attempt to delete `General` → action unavailable (hidden/disabled) with an aria reason.
- Duplicate / empty / whitespace folder name → reject with inline validation, no submit.
- Empty folder → task list shows an empty state, not a blank panel.
- All folders empty → global empty state.
- Count badges and the active placeholder update reactively after every mutation.

## Open Assumptions (resolved)

1. `▼ All folders` = aggregate cross-folder view (FR-11).
2. Filter state is global UI state; persists across folder switches.
3. Counts represent total tasks per folder (not filtered-by-completion counts).

## Definition of Done

- [ ] Domain unit tests green (backend folder/task rules, General-protection, reassignment).
- [ ] API integration tests green (delete-reassignment, General-protection, validation).
- [ ] Frontend unit tests green (store/state reducers, components).
- [ ] Cucumber/e2e scenarios green for every Gherkin scenario above.
- [ ] Wireframe approved (`ui: true`).
- [ ] Mockup approved (`ui: true`).
- [ ] A11y audit passed — WCAG 2.2 AA, both themes.
- [ ] ADR linked (data model + API boundary).
- [ ] `make run` launches the app; `make test-all` green in CI.
- [ ] Docs updated in `/docs` (feature overview + API contract).
- [ ] No AI attribution anywhere.

## ADR Links

- [ADR-0001 — Folder/Task domain model & transactional reassignment](../specs/adrs/0001-folder-task-domain-and-reassignment.md)
