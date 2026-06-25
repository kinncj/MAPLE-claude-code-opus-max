package com.taskmaple.todo.infrastructure.persistence;

import com.taskmaple.todo.domain.model.Task;
import com.taskmaple.todo.domain.repository.TaskRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Adapts Spring Data JPA to the {@link TaskRepository} port. */
@Repository
public class TaskRepositoryAdapter implements TaskRepository {

    private final SpringDataTaskRepository tasks;

    public TaskRepositoryAdapter(SpringDataTaskRepository tasks) {
        this.tasks = tasks;
    }

    @Override
    public List<Task> findByFolderId(UUID folderId) {
        return tasks.findByFolderIdOrderByCreatedAtAscIdAsc(folderId)
            .stream().map(TaskJpaEntity::toDomain).toList();
    }

    @Override
    public List<Task> findAllOrdered() {
        return tasks.findAllByOrderByCreatedAtAscIdAsc()
            .stream().map(TaskJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return tasks.findById(id).map(TaskJpaEntity::toDomain);
    }

    @Override
    public Task save(Task task) {
        return tasks.save(TaskJpaEntity.fromDomain(task)).toDomain();
    }

    @Override
    public void deleteById(UUID id) {
        tasks.deleteById(id);
    }

    @Override
    public Map<UUID, Long> countByFolder() {
        Map<UUID, Long> out = new LinkedHashMap<>();
        for (SpringDataTaskRepository.FolderCount row : tasks.countGroupedByFolder()) {
            out.put(row.getFolderId(), row.getCnt());
        }
        return out;
    }
}
