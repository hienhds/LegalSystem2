package com.example.userservice.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource Not Found"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
    BUSINESS_RULE_VIOLATION(HttpStatus.BAD_REQUEST, "Business rule violation"),

    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Invalidverification token"),

    TOKEN_USED(HttpStatus.BAD_REQUEST, "Token already used"),

    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Token expired"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND,  "Resource not found");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorType(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() { return status; }
    public String getDefaultMessage() { return defaultMessage; }
}
