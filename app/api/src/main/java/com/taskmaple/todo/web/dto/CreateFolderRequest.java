package com.taskmaple.todo.web.dto;

/** Request body for creating a folder. Validation is enforced by the domain. */
public record CreateFolderRequest(String name) {
}
