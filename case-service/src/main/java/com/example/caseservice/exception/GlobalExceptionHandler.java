package com.example.caseservice.exception;

import com.example.caseservice.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(e.getErrorType().getStatus().value())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(e.getErrorType().getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(500)
                .message("Lỗi không xác định: " + e.getMessage())
                .build();
        return ResponseEntity.status(500).body(response);
    }
}