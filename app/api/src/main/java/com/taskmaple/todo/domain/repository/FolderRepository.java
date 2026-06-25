package com.taskmaple.todo.domain.repository;

import com.taskmaple.todo.domain.model.Folder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Outbound port for folder persistence. Implemented by the infrastructure layer. */
public interface FolderRepository {

    /** All folders, default ("General") first, then by createdAt ascending. */
    List<Folder> findAllOrdered();

    Optional<Folder> findById(UUID id);

    /** The protected default folder, if it exists. */
    Optional<Folder> findDefault();

    boolean existsByNameIgnoreCase(String name);

    Folder save(Folder folder);

    /**
     * Atomically resolve the default folder, reassign every task in {@code folderId} to it, then
     * delete the folder — all in a single transaction. Resolving the default inside the transaction
     * closes the time-of-check/time-of-use gap between picking the target and reassigning.
     *
     * @return the number of tasks reassigned and the id of the default folder they moved to
     */
    ReassignmentResult deleteAndReassignToDefault(UUID folderId);

    /** Outcome of {@link #deleteAndReassignToDefault(UUID)}. */
    record ReassignmentResult(int reassignedCount, UUID generalFolderId) {
    }
}
