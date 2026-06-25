---
story: folders-001
title: "TODO SPA — Folders feature (low-fidelity wireframe)"
status: approved
formats: [md, html, excalidraw]
source_image: "Untitled-2026-05-19-1235.excalidraw.png"
---

# Wireframe — TODO SPA, Folders feature

Low-fidelity layout for story `folders-001`. Two-column shell: a **Folders sidebar**
(left) and a **Tasks panel** (right). Source of truth is the attached hand-drawn
wireframe; this file plus `.html` and `.excalidraw` are the canonical, reviewable forms.

## Screen: default (folder "General" active)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  Tasks                                                            [ ☀ / 🌙 ]   │  ← header + theme toggle (FR-10)
├───────────────────────┬────────────────────────────────────────────────────────┤
│  Folders              │  ┌──────────────────────────────────────────────┐  [+] │  ← add-task input (FR-7)
│                       │  │ Add new task in General ...                  │      │     placeholder = active folder
│  ★ General (3)        │  └──────────────────────────────────────────────┘      │
│    Work (1)           │                                                          │
│    Groceries (2)      │  ( All ) ( Todo ) ( Done )      [ Delete folder ]        │  ← filter pills (FR-9) + delete (FR-6)
│                       │  ┌──────────────────────────────────────────────────┐  │
│                       │  │ [ ] Buy milk                              [Del]  │  │  ← task rows (FR-8)
│                       │  │ [x] Walk dog                              [Del]  │  │
│                       │  │ [ ] Write report                          [Del]  │  │
│                       │  │                                                  │  │
│  ┌─────────────────┐  │  │                                                  │  │
│  │  + New folder   │  │  │  Delete folder "Work"? Tasks move to General.    │  │  ← inline confirm (FR-6),
│  └─────────────────┘  │  │                              [Confirm] [Cancel]  │  │     red, scoped to panel
│  ┌─────────────────┐  │  └──────────────────────────────────────────────────┘  │
│  │  ▼ All folders  │  │                                                          │
│  └─────────────────┘  │                                                          │
└───────────────────────┴────────────────────────────────────────────────────────┘
```

### Annotations

| Marker | Element | Requirement | Behaviour |
|--------|---------|-------------|-----------|
| `★` | Active-folder star prefix | FR-2 | Only the active folder is starred + highlighted. `aria-current="true"`. |
| `name (n)` | Folder + count badge | FR-1 | `n` = live task count; updates after every mutation. |
| `+ New folder` | Create button | FR-5 | Reveals an inline name input; on valid submit the new folder becomes active. |
| `▼ All folders` | Aggregate view | FR-11 | Cross-folder list, tasks labeled by folder; delete-folder hidden; add-task defaults to General. |
| `Add new task in <folder> ...` | Add input | FR-7 | Placeholder reflects active folder; `[+]` is an `aria-label="Add task"` icon button. |
| `( All )( Todo )( Done )` | Filter pills | FR-9 | `role="radiogroup"`; selected pill `aria-checked`. Persists across folder switches. |
| `[ Delete folder ]` | Delete action | FR-6 | Hidden/disabled when active folder is `General` or in All-folders view; `aria-disabled` + tooltip reason. |
| `[ ] / [x]` | Done checkbox | FR-8 | Native checkbox; `aria-label="Mark <title> done"`. |
| `[Del]` | Delete task | FR-8 | Icon button, `aria-label="Delete <title>"`. |
| `[ ☀ / 🌙 ]` | Theme toggle | FR-10 | `aria-label="Toggle dark theme"`, `aria-pressed`. Persists in localStorage. |

## Screen states

1. **Default** — General active, 3 tasks, filter = All (above).
2. **Folder selected (Work)** — list shows only Work tasks; placeholder `Add new task in Work ...`; Delete folder visible.
3. **Inline delete confirmation** — red prompt `Delete folder "Work"? Tasks move to General.` with `[Confirm] [Cancel]`. `[Cancel]` is the default/focused safe action; `Esc` cancels.
4. **New-folder entry** — inline text field replaces the `+ New folder` button label; invalid (empty/duplicate) shows an inline validation message, no submit.
5. **All folders** — aggregate; each task labeled with its folder; delete-folder hidden.
6. **Empty folder** — task panel shows an empty state ("No tasks in <folder> yet"), not a blank panel.
7. **All-empty** — global empty state.
8. **Dark theme** — same layout, dark palette; contrast AA in both themes.

## Focus order (keyboard)

theme toggle → add-task input → `[+]` → filter pills (radiogroup, arrow keys) → delete-folder →
folder list (sidebar) → `+ New folder` → `▼ All folders` → task rows (checkbox → `[Del]`).
On opening the delete confirmation, focus moves to `[Cancel]`; on close, focus returns to `[Delete folder]`.
