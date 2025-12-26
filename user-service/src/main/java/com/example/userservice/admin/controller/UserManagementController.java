package com.example.userservice.admin.controller;

import com.example.userservice.admin.dto.UserManagementResponse;
import com.example.userservice.admin.service.UserManagementService;
import com.example.userservice.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean status,
            HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User accessing user management: {}", auth.getName());
        log.info("Authorities: {}", auth.getAuthorities());
        
        Page<UserManagementResponse> users = userManagementService.getAllUsers(page, size, search, status);

        ApiResponse<Page<UserManagementResponse>> response = ApiResponse.<Page<UserManagementResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách người dùng thành công")
                .data(users)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(
            @PathVariable Long id,
            HttpServletRequest request) {

        userManagementService.lockUser(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Khóa người dùng thành công")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(
            @PathVariable Long id,
            HttpServletRequest request) {

        userManagementService.unlockUser(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Mở khóa người dùng thành công")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            HttpServletRequest request) {

        userManagementService.deleteUser(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Xóa người dùng thành công")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }
}
