package com.taskmaple.todo.web.dto;

import java.util.UUID;

/** Request body for creating a task in a folder. */
public record CreateTaskRequest(UUID folderId, String title) {
}
