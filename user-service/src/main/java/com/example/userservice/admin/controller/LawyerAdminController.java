package com.example.userservice.admin.controller;


import com.example.userservice.common.dto.ApiResponse;
import com.example.userservice.common.security.CustomUserDetails;
import com.example.userservice.lawyer.dto.request.FilterLawyerRequest;
import com.example.userservice.lawyer.dto.response.LawyerDetailResponse;
import com.example.userservice.lawyer.dto.response.LawyerListResponse;
import com.example.userservice.lawyer.entity.VerificationStatus;
import com.example.userservice.lawyer.service.LawyerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/lawyers")
@RequiredArgsConstructor
public class LawyerAdminController {

    private final LawyerService lawyerService;

    // 1. Get list
    @GetMapping
    public ResponseEntity<ApiResponse<Page<LawyerListResponse>>> getAllLawyers(
            FilterLawyerRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest servletRequest
    ) {

        Page<LawyerListResponse> lawyerList = lawyerService.getAllLawyers(filter, page, size);

        ApiResponse<Page<LawyerListResponse>> response =
                ApiResponse.<Page<LawyerListResponse>>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Lawyer list retrieved successfully")
                        .data(lawyerList)
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(UUID.randomUUID().toString())
                        .build();

        return ResponseEntity.ok(response);
    }


    // 2. Get detail
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LawyerDetailResponse>> getDetail(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        LawyerDetailResponse detail = lawyerService.getDetail(id);

        ApiResponse<LawyerDetailResponse> response =
                ApiResponse.<LawyerDetailResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Lawyer detail retrieved successfully")
                        .data(detail)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(UUID.randomUUID().toString())
                        .build();

        return ResponseEntity.ok(response);
    }


    // 3. Update status
    @PutMapping("/{lawyerId}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long lawyerId,
            @RequestParam VerificationStatus status,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long adminId = user.getUser().getUserId();
        String msg = lawyerService.updateStatus(lawyerId, status, adminId);

        ApiResponse<String> response =
                ApiResponse.<String>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Status updated successfully")
                        .data(msg)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(UUID.randomUUID().toString())
                        .build();

        return ResponseEntity.ok(response);
    }
    
    // 4. Approve lawyer (verify)
    @PutMapping("/{lawyerId}/approve")
    public ResponseEntity<ApiResponse<String>> approveLawyer(
            @PathVariable Long lawyerId,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long adminId = user.getUser().getUserId();
        String msg = lawyerService.updateStatus(lawyerId, VerificationStatus.APPROVED, adminId);

        ApiResponse<String> response =
                ApiResponse.<String>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Xác minh luật sư thành công")
                        .data(msg)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(UUID.randomUUID().toString())
                        .build();

        return ResponseEntity.ok(response);
    }

    // 5. Reject lawyer
    @PutMapping("/{lawyerId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectLawyer(
            @PathVariable Long lawyerId,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long adminId = user.getUser().getUserId();
        String msg = lawyerService.updateStatus(lawyerId, VerificationStatus.REJECTED, adminId);

        ApiResponse<String> response =
                ApiResponse.<String>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Từ chối xác minh luật sư thành công")
                        .data(msg)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(UUID.randomUUID().toString())
                        .build();

        return ResponseEntity.ok(response);
    }

    // 6. Delete lawyer
    @DeleteMapping("/{lawyerId}")
    public ResponseEntity<ApiResponse<Void>> deleteLawyer(
            @PathVariable Long lawyerId,
            HttpServletRequest request
    ) {
        lawyerService.deleteLawyer(lawyerId);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Xóa luật sư thành công")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .traceId(UUID.randomUUID().toString())
                        .build();

        return ResponseEntity.ok(response);
    }
}
