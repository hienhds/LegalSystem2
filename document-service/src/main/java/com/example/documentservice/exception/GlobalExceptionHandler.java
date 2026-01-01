package com.example.documentservice.exception;


import com.example.documentservice.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1️⃣ Xử lý lỗi do dev ném ra (AppException)
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex, HttpServletRequest request) {
        ErrorType type = ex.getType();
        String traceId = UUID.randomUUID().toString();

        log.warn("[{}] [{}] {} - {}", traceId, type.name(), request.getRequestURI(), ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .status(type.getStatus().value())
                .errorCode(type.name())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(type.getStatus()).body(response);
    }

    /**
     * 2️⃣ Lỗi xác thực / đăng nhập (BadCredentials, Locked, Disabled)
     */
    @ExceptionHandler({
            BadCredentialsException.class,
            LockedException.class,
            DisabledException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleAuthExceptions(Exception ex, HttpServletRequest request) {
        String msg;
        ErrorType type;

        if (ex instanceof LockedException) {
            msg = "Tài khoản của bạn đã bị khóa!";
            type = ErrorType.FORBIDDEN;
        } else if (ex instanceof DisabledException) {
            msg = "Tài khoản của bạn chưa được kích hoạt!";
            type = ErrorType.FORBIDDEN;
        } else {
            msg = "Email hoặc mật khẩu không đúng!";
            type = ErrorType.UNAUTHORIZED;
        }

        String traceId = UUID.randomUUID().toString();
        log.warn("[AUTH] [{}] {} - {}", traceId, request.getRequestURI(), msg);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .status(type.getStatus().value())
                .errorCode(type.name())
                .message(msg)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(type.getStatus()).body(response);
    }

    /**
     * 3️⃣ Lỗi validate DTO (Spring Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String traceId = UUID.randomUUID().toString();
        log.warn("[VALIDATION] [{}] {} - {}", traceId, request.getRequestURI(), errors);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .status(ErrorType.VALIDATION_ERROR.getStatus().value())
                .errorCode(ErrorType.VALIDATION_ERROR.name())
                .message("Dữ liệu không hợp lệ!")
                .errors(errors)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(ErrorType.VALIDATION_ERROR.getStatus()).body(response);
    }

    /**
     * 4️⃣ Lỗi JSON parse hoặc sai format input
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[BAD_REQUEST] [{}] {} - {}", traceId, request.getRequestURI(), ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .status(ErrorType.BAD_REQUEST.getStatus().value())
                .errorCode(ErrorType.BAD_REQUEST.name())
                .message("Payload không hợp lệ hoặc thiếu dữ liệu bắt buộc!")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(ErrorType.BAD_REQUEST.getStatus()).body(response);
    }

    /**
     * 5️⃣ Lỗi ResponseStatusException từ Spring
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[SPRING] [{}] {} - {}", traceId, request.getRequestURI(), ex.getMessage());

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .status(ex.getStatusCode().value())
                .errorCode(ex.getStatusCode().toString())
                .message(ex.getReason())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    /**
     * 6️⃣ Lỗi không xác định (Exception)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllUncaughtExceptions(Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("[UNHANDLED] [{}] {} - {}", traceId, request.getRequestURI(), ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .status(ErrorType.INTERNAL_ERROR.getStatus().value())
                .errorCode(ErrorType.INTERNAL_ERROR.name())
                .message("Lỗi hệ thống nội bộ! Vui lòng thử lại sau.")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(ErrorType.INTERNAL_ERROR.getStatus()).body(response);
    }
}
