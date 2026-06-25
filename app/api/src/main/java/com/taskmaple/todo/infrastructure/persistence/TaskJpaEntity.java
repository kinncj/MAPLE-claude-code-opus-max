package com.taskmaple.todo.infrastructure.persistence;

import com.taskmaple.todo.domain.model.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA persistence model for a task. Kept separate from the domain {@link Task}. */
@Entity
@Table(name = "tasks", indexes = @Index(name = "idx_tasks_folder", columnList = "folder_id"))
public class TaskJpaEntity {

    @Id
    private UUID id;

    @Column(name = "folder_id", nullable = false)
    private UUID folderId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false)
    private boolean done;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TaskJpaEntity() {
    }

    public TaskJpaEntity(UUID id, UUID folderId, String title, boolean done, Instant createdAt) {
        this.id = id;
        this.folderId = folderId;
        this.title = title;
        this.done = done;
        this.createdAt = createdAt;
    }

    public static TaskJpaEntity fromDomain(Task t) {
        return new TaskJpaEntity(t.id(), t.folderId(), t.title(), t.done(), t.createdAt());
    }

    public Task toDomain() {
        return Task.reconstitute(id, folderId, title, done, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDone() {
        return done;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
