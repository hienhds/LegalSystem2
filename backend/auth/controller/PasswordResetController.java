package com.example.backend.auth.controller;


import com.example.backend.auth.dto.request.ForgotPasswordRequest;
import com.example.backend.auth.dto.request.ResetPasswordRequest;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.auth.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// ...existing code...
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody ForgotPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        passwordResetService.sendResetPasswordLink(request.getEmail());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Yêu cầu đặt lại mật khẩu đã được gửi đến email của bạn.")
                .data(null)
                .errorCode(null)
                .errors(null)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .traceId(UUID.randomUUID().toString())
                .links(Map.of(
                        "resetPassword", "/api/auth/reset-password"
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reset-password/validate")
    public String validateToken(@RequestParam String token) {
        String redirectUrl = passwordResetService.validateResetToken(token);
        return "redirect:" + redirectUrl;
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody ResetPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.")
                .data(null)
                .errorCode(null)
                .errors(null)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .traceId(UUID.randomUUID().toString())
                .links(Map.of(
                        "login", "/api/auth/login"
                ))
                .build();

        return ResponseEntity.ok(response);
    }
}