package com.taskmaple.todo.application;

import com.taskmaple.todo.application.port.AuditLog;
import com.taskmaple.todo.application.view.DeleteFolderResult;
import com.taskmaple.todo.application.view.FolderView;
import com.taskmaple.todo.domain.error.DomainExceptions;
import com.taskmaple.todo.domain.model.Folder;
import com.taskmaple.todo.domain.repository.FolderRepository;
import com.taskmaple.todo.domain.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Folder use cases. Framework-free; depends only on domain ports.
 * Enforces case-insensitive uniqueness, default-folder protection, and orchestrates
 * transactional reassignment-on-delete.
 */
public class FolderService {

    private final FolderRepository folders;
    private final TaskRepository tasks;
    private final AuditLog audit;

    public FolderService(FolderRepository folders, TaskRepository tasks, AuditLog audit) {
        this.folders = folders;
        this.tasks = tasks;
        this.audit = audit;
    }

    public List<FolderView> listFolders() {
        Map<UUID, Long> counts = tasks.countByFolder();
        List<FolderView> out = new ArrayList<>();
        for (Folder f : folders.findAllOrdered()) {
            out.add(new FolderView(f.id(), f.name(), f.isDefault(), f.createdAt(),
                counts.getOrDefault(f.id(), 0L)));
        }
        return out;
    }

    public FolderView createFolder(String rawName) {
        String name = Folder.normalizeName(rawName);
        if (folders.existsByNameIgnoreCase(name)) {
            throw new DomainExceptions.DuplicateFolderName(name);
        }
        Folder folder = folders.save(Folder.create(name));
        audit.folderCreated(folder);
        return new FolderView(folder.id(), folder.name(), folder.isDefault(), folder.createdAt(), 0L);
    }

    public DeleteFolderResult deleteFolder(UUID id) {
        Folder folder = folders.findById(id)
            .orElseThrow(() -> new DomainExceptions.FolderNotFound(id));
        if (folder.isDefault()) {
            throw new DomainExceptions.DefaultFolderProtected();
        }

        var result = folders.deleteAndReassignToDefault(id);
        audit.folderDeleted(id, folder.name(), result.reassignedCount(), result.generalFolderId());
        return new DeleteFolderResult(id, result.reassignedCount(), result.generalFolderId());
    }
}
