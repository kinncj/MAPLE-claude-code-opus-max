package com.taskmaple.todo.domain.repository;

import com.taskmaple.todo.domain.model.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Outbound port for task persistence. Implemented by the infrastructure layer. */
public interface TaskRepository {

    /** Tasks in one folder, newest first. */
    List<Task> findByFolderId(UUID folderId);

    /** All tasks across every folder, newest first (All-folders view). */
    List<Task> findAllOrdered();

    Optional<Task> findById(UUID id);

    Task save(Task task);

    void deleteById(UUID id);

    /** Task count per folder id, used to build folder badges without N+1 queries. */
    Map<UUID, Long> countByFolder();
}
