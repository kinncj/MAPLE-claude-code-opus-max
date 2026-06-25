package com.taskmaple.todo.application.view;

import java.util.UUID;

/** Result of deleting a folder: how many tasks moved, and where they went. */
public record DeleteFolderResult(UUID deletedFolderId, int reassignedCount, UUID generalFolderId) {
}
