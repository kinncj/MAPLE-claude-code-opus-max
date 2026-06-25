package com.taskmaple.todo.infrastructure.support;

import com.taskmaple.todo.domain.model.Folder;
import com.taskmaple.todo.domain.model.Task;
import com.taskmaple.todo.infrastructure.persistence.FolderJpaEntity;
import com.taskmaple.todo.infrastructure.persistence.SpringDataFolderRepository;
import com.taskmaple.todo.infrastructure.persistence.SpringDataTaskRepository;
import com.taskmaple.todo.infrastructure.persistence.TaskJpaEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Bootstrap and demo/test seeding. Centralises the "General exists" invariant and the sample
 * dataset that mirrors the wireframe and the Gherkin Background
 * (General=3 tasks, Work=1, Groceries=2).
 */
@Component
public class SeedService {

    private final SpringDataFolderRepository folders;
    private final SpringDataTaskRepository tasks;

    public SeedService(SpringDataFolderRepository folders, SpringDataTaskRepository tasks) {
        this.folders = folders;
        this.tasks = tasks;
    }

    /** Create the protected default folder if absent. Idempotent. */
    @Transactional
    public FolderJpaEntity ensureDefault() {
        return folders.findFirstByIsDefaultTrue()
            .orElseGet(() -> folders.save(FolderJpaEntity.fromDomain(Folder.createDefault())));
    }

    /** Seed the sample dataset only when no user folders exist yet. */
    @Transactional
    public void seedSampleIfEmpty() {
        FolderJpaEntity general = ensureDefault();
        boolean onlyDefault = folders.count() <= 1;
        boolean generalEmpty = tasks.findByFolderIdOrderByCreatedAtAscIdAsc(general.getId()).isEmpty();
        if (onlyDefault && generalEmpty) {
            seedBackground(general.getId());
        }
    }

    /** Wipe everything and re-seed the canonical Background state (used by test support). */
    @Transactional
    public void resetToBackground() {
        tasks.deleteAllInBatch();
        folders.deleteAllInBatch();
        FolderJpaEntity general = folders.save(FolderJpaEntity.fromDomain(Folder.createDefault()));
        seedBackground(general.getId());
    }

    private void seedBackground(UUID generalId) {
        Instant base = Instant.now();
        int n = 0;

        saveTask(generalId, "Buy milk", false, base, n++);
        saveTask(generalId, "Walk dog", true, base, n++);
        saveTask(generalId, "Write report", false, base, n++);

        UUID workId = saveFolder("Work", base, n++).getId();
        saveTask(workId, "Prepare release deck", false, base, n++);

        UUID grocId = saveFolder("Groceries", base, n++).getId();
        saveTask(grocId, "Apples", false, base, n++);
        saveTask(grocId, "Bread", false, base, n++);
    }

    private FolderJpaEntity saveFolder(String name, Instant base, int offset) {
        Folder f = Folder.reconstitute(UUID.randomUUID(), name, false, base.plusMillis(offset));
        return folders.save(FolderJpaEntity.fromDomain(f));
    }

    private void saveTask(UUID folderId, String title, boolean done, Instant base, int offset) {
        Task t = Task.reconstitute(UUID.randomUUID(), folderId, title, done, base.plusMillis(offset));
        tasks.save(TaskJpaEntity.fromDomain(t));
    }
}
