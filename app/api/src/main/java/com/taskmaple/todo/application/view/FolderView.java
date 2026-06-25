package com.taskmaple.todo.application.view;

import java.time.Instant;
import java.util.UUID;

/** Read model: a folder plus its live task count. */
public record FolderView(UUID id, String name, boolean isDefault, Instant createdAt, long taskCount) {
}
