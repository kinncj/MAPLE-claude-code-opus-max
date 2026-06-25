package com.taskmaple.todo.web.dto;

/** Partial update for a task: toggle done and/or rename. Null fields are left unchanged. */
public record UpdateTaskRequest(Boolean done, String title) {
}
