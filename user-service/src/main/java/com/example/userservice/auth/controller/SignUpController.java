package com.example.userservice.auth.controller;

import com.example.userservice.auth.dto.request.RegisterRequest;
import com.example.userservice.auth.entity.UserToken;
import com.example.userservice.auth.service.SignUpService;
import com.example.userservice.auth.service.TokenService;
import com.example.userservice.common.dto.ApiResponse;
import com.example.userservice.user.dto.UserMapper;
import com.example.userservice.user.dto.UserResponse;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class SignUpController {

    private final SignUpService signUpService;

    private final TokenService tokenService;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest
    ) {
        String traceId = UUID.randomUUID().toString();
        log.info("[REGISTER] [{}] Received registration request for email: {}", traceId, request.getEmail());

        // Xử lý đăng ký
        User user = signUpService.register(request);
        UserResponse userResponse = userMapper.userToRegistrationResponse(user);

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.")
                .data(userResponse)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .traceId(traceId)
                .links(Map.of(
                        "self", servletRequest.getRequestURL().toString(),
                        "verify", "/api/auth/verify?token={token}"
                ))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Xác minh email qua token
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(
            @RequestParam("token") String token,
            HttpServletRequest servletRequest
    ) {
        String traceId = UUID.randomUUID().toString();
        log.info("[VERIFY] [{}] Received email verification for token: {}", traceId, token);

        // Kiểm tra token hợp lệ
        UserToken userToken = tokenService.validateVerificationToken(token);
        User user = userToken.getUser();

        user.setActive(true);
        userRepository.save(user);

        log.info("[VERIFY] [{}] User verified successfully: {}", traceId, user.getEmail());

        UserResponse userResponse = userMapper.userToRegistrationResponse(user);

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Tài khoản đã được xác minh thành công.")
                .data(userResponse)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .traceId(traceId)
                .links(Map.of(
                        "self", servletRequest.getRequestURL().toString(),
                        "login", "/api/auth/login"
                ))
                .build();

        return ResponseEntity.ok(response);
    }
}
