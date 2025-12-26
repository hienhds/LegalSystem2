package com.example.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base response wrapper cho tất cả API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    
    private boolean success;
    private int status;
    private String message;
    private T data;
    private String errorCode;
    private LocalDateTime timestamp;
    private String path;
    
    // Static factory methods
    public static <T> BaseResponse<T> success(String message, T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> BaseResponse<T> success(T data) {
        return success("Operation successful", data);
    }
    
    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .status(500)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> BaseResponse<T> error(int status, String message, String errorCode) {
        return BaseResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}