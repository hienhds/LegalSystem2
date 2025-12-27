package com.example.chatservice.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException{
    private final ErrorType type;

    public AppException(ErrorType type) {
        super(type.getDefaultMessage());
        this.type = type;
    }

    public AppException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }
}
