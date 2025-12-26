package com.example.backend.case_management.controller;

import com.example.backend.case_management.dto.CaseResponse;
import com.example.backend.case_management.dto.CaseUpdateResponse;
import com.example.backend.case_management.dto.CreateCaseRequest;
import com.example.backend.case_management.dto.UpdateProgressRequest;
import com.example.backend.case_management.service.CaseService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    // 1. TẠO VỤ ÁN (Đã sửa OK)
    @PostMapping
    public ResponseEntity<ApiResponse<CaseResponse>> createCase(
            @RequestBody CreateCaseRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest
    ) {
        Long clientId = userDetails.getUser().getUserId();
        CaseResponse caseResponse = caseService.createCase(clientId, request);

        ApiResponse<CaseResponse> response = ApiResponse.<CaseResponse>builder()
                .success(true)
                .message("Tạo vụ án thành công")
                .data(caseResponse)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. LẤY CHI TIẾT VỤ ÁN (Cần sửa chỗ này thì mới test được)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseResponse>> getCase(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        // GỌI SERVICE LẤY CHI TIẾT
        CaseResponse caseDetail = caseService.getCaseDetail(id);

        ApiResponse<CaseResponse> response = ApiResponse.<CaseResponse>builder()
                .success(true)
                .message("Lấy thông tin vụ án thành công")
                .data(caseDetail)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    // 3. CẬP NHẬT TIẾN ĐỘ (Đã OK)
    @PostMapping("/{id}/updates")
    public ResponseEntity<ApiResponse<CaseUpdateResponse>> addUpdate(
            @PathVariable Long id,
            @RequestBody UpdateProgressRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest
    ) {
        Long userId = userDetails.getUser().getUserId();
        CaseUpdateResponse updateResponse = caseService.addCaseUpdate(id, userId, request);

        ApiResponse<CaseUpdateResponse> response = ApiResponse.<CaseUpdateResponse>builder()
                .success(true)
                .message("Cập nhật tiến độ thành công")
                .data(updateResponse)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    // 4. UPLOAD TÀI LIỆU (Đã OK)
    @PostMapping(value = "/{id}/documents", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest
    ) {
        Long userId = userDetails.getUser().getUserId();
        String url = caseService.uploadCaseDocument(id, userId, file);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Upload tài liệu thành công")
                .data(url)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}