package com.taskmaple.todo.domain.error;

/**
 * Base type for business-rule violations. Carries a transport-agnostic {@link ErrorCode}
 * and an optional offending field name. The web layer maps these to HTTP responses.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode code;
    private final String field;

    public DomainException(ErrorCode code, String message, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public ErrorCode code() {
        return code;
    }

    public String field() {
        return field;
    }
}
