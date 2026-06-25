package com.taskmaple.todo.support;

import com.taskmaple.todo.application.port.AuditLog;
import com.taskmaple.todo.domain.model.Folder;
import com.taskmaple.todo.domain.model.Task;
import com.taskmaple.todo.domain.repository.FolderRepository;
import com.taskmaple.todo.domain.repository.TaskRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory test doubles for the domain ports, sharing one backing store. */
public final class InMemoryRepositories {

    public final Map<UUID, Folder> folders = new LinkedHashMap<>();
    public final Map<UUID, Task> tasks = new LinkedHashMap<>();

    public final FolderRepository folderRepository = new FolderRepository() {
        @Override
        public List<Folder> findAllOrdered() {
            Comparator<Folder> byDefault = Comparator.comparing(Folder::isDefault).reversed();
            return folders.values().stream()
                .sorted(byDefault.thenComparing(Folder::createdAt))
                .toList();
        }

        @Override
        public Optional<Folder> findById(UUID id) {
            return Optional.ofNullable(folders.get(id));
        }

        @Override
        public Optional<Folder> findDefault() {
            return folders.values().stream().filter(Folder::isDefault).findFirst();
        }

        @Override
        public boolean existsByNameIgnoreCase(String name) {
            return folders.values().stream().anyMatch(f -> f.name().equalsIgnoreCase(name));
        }

        @Override
        public Folder save(Folder folder) {
            folders.put(folder.id(), folder);
            return folder;
        }

        @Override
        public ReassignmentResult deleteAndReassignToDefault(UUID folderId) {
            UUID generalId = folders.values().stream().filter(Folder::isDefault).findFirst()
                .orElseThrow(() -> new IllegalStateException("Default folder is missing")).id();
            int count = 0;
            for (Task t : tasks.values()) {
                if (t.folderId().equals(folderId)) {
                    t.reassignTo(generalId);
                    count++;
                }
            }
            folders.remove(folderId);
            return new ReassignmentResult(count, generalId);
        }
    };

    public final TaskRepository taskRepository = new TaskRepository() {
        @Override
        public List<Task> findByFolderId(UUID folderId) {
            return tasks.values().stream()
                .filter(t -> t.folderId().equals(folderId))
                .sorted(Comparator.comparing(Task::createdAt))
                .toList();
        }

        @Override
        public List<Task> findAllOrdered() {
            return tasks.values().stream().sorted(Comparator.comparing(Task::createdAt)).toList();
        }

        @Override
        public Optional<Task> findById(UUID id) {
            return Optional.ofNullable(tasks.get(id));
        }

        @Override
        public Task save(Task task) {
            tasks.put(task.id(), task);
            return task;
        }

        @Override
        public void deleteById(UUID id) {
            tasks.remove(id);
        }

        @Override
        public Map<UUID, Long> countByFolder() {
            Map<UUID, Long> out = new LinkedHashMap<>();
            for (Task t : tasks.values()) {
                out.merge(t.folderId(), 1L, Long::sum);
            }
            return out;
        }
    };

    /** AuditLog that counts invocations for assertions. */
    public static final class RecordingAuditLog implements AuditLog {
        public int created;
        public int deleted;
        public int lastReassignedCount;

        @Override
        public void folderCreated(Folder folder) {
            created++;
        }

        @Override
        public void folderDeleted(UUID folderId, String name, int reassignedCount, UUID generalFolderId) {
            deleted++;
            lastReassignedCount = reassignedCount;
        }
    }

    /** Seed and return the default General folder. */
    public Folder seedGeneral() {
        Folder general = Folder.createDefault();
        folders.put(general.id(), general);
        return general;
    }

    public Folder seedFolder(String name) {
        Folder f = Folder.create(name);
        folders.put(f.id(), f);
        return f;
    }

    public Task seedTask(UUID folderId, String title, boolean done) {
        Task t = Task.create(folderId, title);
        t.markDone(done);
        tasks.put(t.id(), t);
        return t;
    }
}
