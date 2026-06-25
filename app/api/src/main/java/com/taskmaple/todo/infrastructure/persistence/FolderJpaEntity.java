package com.taskmaple.todo.infrastructure.persistence;

import com.taskmaple.todo.domain.model.Folder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/** JPA persistence model for a folder. Kept separate from the domain {@link Folder}. */
@Entity
@Table(name = "folders")
public class FolderJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    /** Lower-cased name backing the case-insensitive uniqueness constraint. */
    @Column(name = "name_ci", nullable = false, unique = true, length = 50)
    private String nameCi;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    /**
     * Non-null only for the default folder ("DEFAULT"). A UNIQUE constraint on this column allows
     * many NULLs but a single non-null value, guaranteeing exactly one default folder at the DB level.
     */
    @Column(name = "default_marker", unique = true)
    private String defaultMarker;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FolderJpaEntity() {
    }

    public FolderJpaEntity(UUID id, String name, boolean isDefault, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.nameCi = name.toLowerCase(Locale.ROOT);
        this.isDefault = isDefault;
        this.defaultMarker = isDefault ? "DEFAULT" : null;
        this.createdAt = createdAt;
    }

    public static FolderJpaEntity fromDomain(Folder f) {
        return new FolderJpaEntity(f.id(), f.name(), f.isDefault(), f.createdAt());
    }

    public Folder toDomain() {
        return Folder.reconstitute(id, name, isDefault, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
