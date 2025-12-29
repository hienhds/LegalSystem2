package com.example.caseservice.exception;

import com.example.caseservice.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        log.error("AppException: {}", e.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(e.getErrorType().getStatus().value())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(e.getErrorType().getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        log.error("Unexpected error occurred: ", e);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(500)
                .message("Lỗi hệ thống: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()))
                .build();
        return ResponseEntity.status(500).body(response);
    }
}