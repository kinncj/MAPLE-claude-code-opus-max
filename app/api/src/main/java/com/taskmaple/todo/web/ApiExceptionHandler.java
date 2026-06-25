package com.taskmaple.todo.web;

import com.taskmaple.todo.domain.error.DomainException;
import com.taskmaple.todo.domain.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Maps domain and framework exceptions to the {@link ApiError} envelope. No stack traces leak. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex) {
        HttpStatus status = statusFor(ex.code());
        return ResponseEntity.status(status)
            .body(ApiError.of(ex.code().name(), ex.getMessage(), ex.field()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
            .body(ApiError.of(ErrorCode.VALIDATION.name(), "Malformed or missing request body.", null));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
            .body(ApiError.of(ErrorCode.VALIDATION.name(), "Invalid value for '" + ex.getName() + "'.", ex.getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError.of("INTERNAL", "An unexpected error occurred.", null));
    }

    private static HttpStatus statusFor(ErrorCode code) {
        return switch (code) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_NAME, CANNOT_DELETE_DEFAULT -> HttpStatus.CONFLICT;
        };
    }
}
