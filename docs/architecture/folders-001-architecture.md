# folders-001 — Architecture

Story: `folders-001`. Stack: Java 21 + Spring Boot 3.3 (H2 in-memory) · Vite + React + TypeScript.
Layering: hexagonal (domain ← application ← {web, infrastructure}). See ADR-0001.

## Component view

```mermaid
flowchart TD
  subgraph Web["app/web — React SPA"]
    UI["Components: Sidebar, TaskList, AddTask, FilterPills, DeleteConfirm, ThemeToggle"]
    Store["State store (useReducer) + api client"]
    UI --> Store
  end
  subgraph Api["app/api — Spring Boot"]
    subgraph WL["web layer"]
      FC["FolderController"]
      TC["TaskController"]
      EH["ApiExceptionHandler"]
    end
    subgraph AL["application layer (framework-free)"]
      FS["FolderService"]
      TS["TaskService"]
    end
    subgraph DL["domain layer (pure)"]
      FM["Folder, Task"]
      PORTS["FolderRepository, TaskRepository (ports)"]
    end
    subgraph IL["infrastructure layer"]
      FA["FolderRepositoryAdapter @Transactional"]
      TA["TaskRepositoryAdapter"]
      H2[("H2 in-memory")]
    end
    BOOT["DataInitializer — creates General"]
  end
  Store -->|"/api/v1"| FC
  Store -->|"/api/v1"| TC
  FC --> FS --> PORTS
  TC --> TS --> PORTS
  FS -. uses .-> FM
  PORTS <-.implemented by.- FA
  PORTS <-.implemented by.- TA
  FA --> H2
  TA --> H2
  BOOT --> FA
```

## Sequence — delete folder with transactional reassignment (FR-6)

```mermaid
sequenceDiagram
  participant U as User
  participant W as React SPA
  participant C as FolderController
  participant S as FolderService
  participant R as FolderRepositoryAdapter
  participant DB as H2
  U->>W: Confirm "Delete folder Work"
  W->>C: DELETE /api/v1/folders/{id}
  C->>S: deleteFolder(id)
  S->>R: findById(id)
  alt folder is General
    S-->>C: DefaultFolderProtectedException
    C-->>W: 409 CANNOT_DELETE_DEFAULT
  else deletable
    S->>R: deleteFolderAndReassign(id, generalId)
    rect rgb(235,242,255)
    note over R,DB: single @Transactional
    R->>DB: UPDATE tasks SET folder_id=generalId WHERE folder_id=id
    R->>DB: DELETE folder id
    end
    R-->>S: reassignedCount
    S-->>C: DeleteFolderResult
    C-->>W: 200 {reassignedCount, generalFolderId}
    W->>W: set General active, refresh counts
  end
```

## Data model (ER)

```mermaid
erDiagram
  FOLDER ||--o{ TASK : contains
  FOLDER {
    uuid id PK
    string name "trimmed 1-50, unique ci"
    boolean is_default "true only for General"
    timestamp created_at
  }
  TASK {
    uuid id PK
    uuid folder_id FK "not null"
    string title "trimmed 1-500"
    boolean done "default false"
    timestamp created_at
  }
```

## Deployment

```mermaid
flowchart LR
  Dev["make run"] --> DC["docker compose (infra/)"]
  DC --> IMG["multi-stage image"]
  subgraph IMG
    direction TB
    S1["stage 1 node: vite build → /dist"]
    S2["stage 2 maven: package jar, embeds /dist as static"]
    S3["stage 3 temurin-jre: java -jar app.jar :8080"]
    S1 --> S2 --> S3
  end
  IMG --> APP["app :8080 — serves SPA + /api/v1, H2 in-memory"]
```

The SPA is served as static resources by Spring Boot, so the browser, API, and store share one
origin (`:8080`) — no CORS, no proxy. `make test-e2e` drives this running container with Playwright.
