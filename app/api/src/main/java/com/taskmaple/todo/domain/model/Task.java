package com.taskmaple.todo.domain.model;

import com.taskmaple.todo.domain.error.DomainExceptions;

import java.time.Instant;
import java.util.UUID;

/**
 * A single to-do item. Belongs to exactly one folder at all times (folderId is never null).
 * Pure domain type — no framework or persistence imports.
 */
public final class Task {

    public static final int TITLE_MIN = 1;
    public static final int TITLE_MAX = 500;

    private final UUID id;
    private UUID folderId;
    private String title;
    private boolean done;
    private final Instant createdAt;

    private Task(UUID id, UUID folderId, String title, boolean done, Instant createdAt) {
        this.id = id;
        this.folderId = folderId;
        this.title = title;
        this.done = done;
        this.createdAt = createdAt;
    }

    public static Task create(UUID folderId, String rawTitle) {
        if (folderId == null) {
            throw new DomainExceptions.InvalidTaskTitle("A task must belong to a folder.");
        }
        return new Task(UUID.randomUUID(), folderId, normalizeTitle(rawTitle), false, Instant.now());
    }

    public static Task reconstitute(UUID id, UUID folderId, String title, boolean done, Instant createdAt) {
        return new Task(id, folderId, title, done, createdAt);
    }

    public void markDone(boolean value) {
        this.done = value;
    }

    public void rename(String rawTitle) {
        this.title = normalizeTitle(rawTitle);
    }

    public void reassignTo(UUID newFolderId) {
        if (newFolderId == null) {
            throw new DomainExceptions.InvalidTaskTitle("A task must belong to a folder.");
        }
        this.folderId = newFolderId;
    }

    public static String normalizeTitle(String rawTitle) {
        String trimmed = rawTitle == null ? "" : rawTitle.trim();
        if (trimmed.isEmpty()) {
            throw new DomainExceptions.InvalidTaskTitle("Task title must not be empty.");
        }
        if (trimmed.length() > TITLE_MAX) {
            throw new DomainExceptions.InvalidTaskTitle(
                "Task title must be at most " + TITLE_MAX + " characters.");
        }
        return trimmed;
    }

    public UUID id() {
        return id;
    }

    public UUID folderId() {
        return folderId;
    }

    public String title() {
        return title;
    }

    public boolean done() {
        return done;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
