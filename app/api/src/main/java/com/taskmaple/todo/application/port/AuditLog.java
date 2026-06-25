package com.taskmaple.todo.application.port;

import com.taskmaple.todo.domain.model.Folder;

import java.util.UUID;

/**
 * Outbound port for structured observability of folder lifecycle events.
 * Keeps the application layer free of any logging framework import.
 */
public interface AuditLog {

    void folderCreated(Folder folder);

    void folderDeleted(UUID folderId, String name, int reassignedCount, UUID generalFolderId);
}
