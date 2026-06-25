package com.taskmaple.todo.application.view;

import java.time.Instant;
import java.util.UUID;

/** Read model: a task labeled with its folder name (for the All-folders view and badges). */
public record TaskView(UUID id, UUID folderId, String folderName, String title, boolean done, Instant createdAt) {
}
