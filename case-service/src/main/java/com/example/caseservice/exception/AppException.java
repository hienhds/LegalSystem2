package com.example.caseservice.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorType errorType;

    public AppException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}