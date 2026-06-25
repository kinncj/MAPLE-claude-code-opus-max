package com.taskmaple.todo.web;

/** Stable error envelope: {@code { "error": { code, message, field } }}. */
public record ApiError(Body error) {

    public record Body(String code, String message, String field) {
    }

    public static ApiError of(String code, String message, String field) {
        return new ApiError(new Body(code, message, field));
    }
}
