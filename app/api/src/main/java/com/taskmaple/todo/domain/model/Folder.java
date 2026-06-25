package com.taskmaple.todo.domain.model;

import com.taskmaple.todo.domain.error.DomainExceptions;

import java.time.Instant;
import java.util.UUID;

/**
 * A user-defined grouping of tasks. Pure domain type — no framework or persistence imports.
 * Invariants (trimmed name, length 1..50) are enforced at construction and rename.
 */
public final class Folder {

    public static final String DEFAULT_NAME = "General";
    public static final int NAME_MIN = 1;
    public static final int NAME_MAX = 50;

    private final UUID id;
    private String name;
    private final boolean isDefault;
    private final Instant createdAt;

    private Folder(UUID id, String name, boolean isDefault, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }

    /** Create a normal, user-created folder. */
    public static Folder create(String rawName) {
        return new Folder(UUID.randomUUID(), normalizeName(rawName), false, Instant.now());
    }

    /** Create the protected default "General" folder. */
    public static Folder createDefault() {
        return new Folder(UUID.randomUUID(), DEFAULT_NAME, true, Instant.now());
    }

    /** Rebuild from persisted state without re-validating (data already accepted). */
    public static Folder reconstitute(UUID id, String name, boolean isDefault, Instant createdAt) {
        return new Folder(id, name, isDefault, createdAt);
    }

    public void rename(String rawName) {
        this.name = normalizeName(rawName);
    }

    /** Trim and validate a candidate folder name; returns the normalized value. */
    public static String normalizeName(String rawName) {
        String trimmed = rawName == null ? "" : rawName.trim();
        if (trimmed.isEmpty()) {
            throw new DomainExceptions.InvalidFolderName("Folder name must not be empty.");
        }
        if (trimmed.length() > NAME_MAX) {
            throw new DomainExceptions.InvalidFolderName(
                "Folder name must be at most " + NAME_MAX + " characters.");
        }
        return trimmed;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
