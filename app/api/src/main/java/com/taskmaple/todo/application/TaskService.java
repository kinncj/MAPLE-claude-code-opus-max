package com.taskmaple.todo.application;

import com.taskmaple.todo.application.view.TaskView;
import com.taskmaple.todo.domain.error.DomainException;
import com.taskmaple.todo.domain.error.DomainExceptions;
import com.taskmaple.todo.domain.error.ErrorCode;
import com.taskmaple.todo.domain.model.Folder;
import com.taskmaple.todo.domain.model.Task;
import com.taskmaple.todo.domain.repository.FolderRepository;
import com.taskmaple.todo.domain.repository.TaskRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Task use cases. Framework-free; depends only on domain ports.
 * Resolves the owning folder name onto each returned view so the SPA can label tasks
 * in the All-folders aggregate.
 */
public class TaskService {

    private final TaskRepository tasks;
    private final FolderRepository folders;

    public TaskService(TaskRepository tasks, FolderRepository folders) {
        this.tasks = tasks;
        this.folders = folders;
    }

    /** List tasks; {@code folderId == null} returns the cross-folder aggregate. */
    public List<TaskView> listTasks(UUID folderId) {
        Map<UUID, String> names = folderNames();
        List<Task> source;
        if (folderId == null) {
            source = tasks.findAllOrdered();
        } else {
            if (!names.containsKey(folderId)) {
                throw new DomainExceptions.FolderNotFound(folderId);
            }
            source = tasks.findByFolderId(folderId);
        }
        List<TaskView> out = new ArrayList<>(source.size());
        for (Task t : source) {
            out.add(toView(t, names));
        }
        return out;
    }

    public TaskView createTask(UUID folderId, String rawTitle) {
        if (folderId == null) {
            throw new DomainException(ErrorCode.VALIDATION, "folderId is required.", "folderId");
        }
        Folder folder = folders.findById(folderId)
            .orElseThrow(() -> new DomainExceptions.FolderNotFound(folderId));
        Task saved = tasks.save(Task.create(folder.id(), rawTitle));
        return new TaskView(saved.id(), saved.folderId(), folder.name(),
            saved.title(), saved.done(), saved.createdAt());
    }

    public TaskView updateTask(UUID id, Boolean done, String title) {
        Task task = tasks.findById(id)
            .orElseThrow(() -> new DomainExceptions.TaskNotFound(id));
        if (done != null) {
            task.markDone(done);
        }
        if (title != null) {
            task.rename(title);
        }
        Task saved = tasks.save(task);
        return toView(saved, folderNames());
    }

    public void deleteTask(UUID id) {
        if (tasks.findById(id).isEmpty()) {
            throw new DomainExceptions.TaskNotFound(id);
        }
        tasks.deleteById(id);
    }

    private Map<UUID, String> folderNames() {
        Map<UUID, String> names = new HashMap<>();
        for (Folder f : folders.findAllOrdered()) {
            names.put(f.id(), f.name());
        }
        return names;
    }

    private static TaskView toView(Task t, Map<UUID, String> names) {
        return new TaskView(t.id(), t.folderId(), names.getOrDefault(t.folderId(), ""),
            t.title(), t.done(), t.createdAt());
    }
}
