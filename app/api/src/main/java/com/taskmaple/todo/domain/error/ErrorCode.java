package com.taskmaple.todo.domain.error;

/** Stable, transport-agnostic error codes surfaced to clients. */
public enum ErrorCode {
    VALIDATION,
    DUPLICATE_NAME,
    NOT_FOUND,
    CANNOT_DELETE_DEFAULT
}
