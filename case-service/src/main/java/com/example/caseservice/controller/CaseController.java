package com.example.caseservice.controller;

import com.example.caseservice.client.UserClient;
import com.example.caseservice.dto.*;
import com.example.caseservice.service.CaseService;
import com.example.caseservice.exception.AppException;
import com.example.caseservice.exception.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Slf4j
public class CaseController {
    private final CaseService caseService;
    private final UserClient userClient;

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new AppException(ErrorType.UNAUTHORIZED, "Token không hợp lệ hoặc thiếu userId");
        }
        return (Long) userId;
    }

    private String getCurrentUserRole(HttpServletRequest request) {
        Object role = request.getAttribute("userRole");
        // If role not in token, fetch from user service via Feign
        if (role == null) {
            Long userId = getCurrentUserId(request);
            try {
                log.debug("Role not in token, fetching from user-service for userId: {}", userId);
                UserResponse userResponse = userClient.getUserById(userId);
                
                log.debug("=== FEIGN RESPONSE DEBUG ===");
                log.debug("UserResponse object: {}", userResponse);
                log.debug("UserResponse is null: {}", userResponse == null);
                
                if (userResponse != null) {
                    log.debug("UserResponse.getRoles(): {}", userResponse.getRoles());
                    log.debug("Roles is null: {}", userResponse.getRoles() == null);
                    log.debug("Roles isEmpty: {}", userResponse.getRoles() != null ? userResponse.getRoles().isEmpty() : "N/A");
                    
                    if (userResponse.getRoles() != null && !userResponse.getRoles().isEmpty()) {
                        String roles = String.join(",", userResponse.getRoles());
                        log.debug("Fetched roles from user-service: {}", roles);
                        return roles;
                    }
                }
                
                log.warn("No roles found for userId: {}", userId);
                return null;
            } catch (Exception e) {
                log.error("Failed to fetch user role from user-service: {}", e.getMessage(), e);
                throw new AppException(ErrorType.INTERNAL_ERROR, "Không thể xác thực quyền hạn người dùng");
            }
        }
        return (String) role;
    }

    private void requireLawyerRole(String role) {
        // If role is null, we cannot determine - throw error
        if (role == null) {
            throw new AppException(ErrorType.FORBIDDEN, "Không thể xác định quyền hạn từ token. Vui lòng đăng nhập lại.");
        }
        if (!role.toUpperCase().contains("LAWYER")) {
            throw new AppException(ErrorType.FORBIDDEN, "Chỉ luật sư mới có quyền thực hiện hành động này");
        }
    }

    @PostMapping
    public ApiResponse<CaseResponse> createCase(
            @RequestBody CreateCaseRequest request,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        String role = getCurrentUserRole(httpRequest);
        requireLawyerRole(role);

        return ApiResponse.<CaseResponse>builder()
                .code(200)
                .message("Tạo vụ án mới thành công")
                .result(caseService.createCase(request, currentUserId))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<CaseResponse>> getMyCases(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest httpRequest) {

        Long currentUserId = getCurrentUserId(httpRequest);
        String role = getCurrentUserRole(httpRequest);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.<Page<CaseResponse>>builder()
                .code(200)
                .message("Lấy danh sách vụ án thành công")
                .result(caseService.searchMyCases(currentUserId, role, keyword, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CaseResponse> getCaseById(
            @PathVariable("id") Long id,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        
        return ApiResponse.<CaseResponse>builder()
                .code(200)
                .message("Lấy thông tin chi tiết vụ án thành công")
                .result(caseService.getCaseById(id, currentUserId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CaseResponse> updateCase(
            @PathVariable("id") Long id,
            @RequestBody CreateCaseRequest request,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        String role = getCurrentUserRole(httpRequest);
        requireLawyerRole(role);

        return ApiResponse.<CaseResponse>builder()
                .code(200)
                .message("Cập nhật thông tin vụ án thành công")
                .result(caseService.updateCase(id, request, currentUserId))
                .build();
    }

    @GetMapping("/{id}/documents/{docId}/download")
    public ApiResponse<String> getDownloadUrl(
            @PathVariable("id") Long id,
            @PathVariable("docId") Long docId,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        
        return ApiResponse.<String>builder()
                .code(200)
                .message("Lấy link tải tài liệu thành công")
                .result(caseService.getDownloadUrl(id, docId, currentUserId))
                .build();
    }

    @GetMapping("/{id}/documents/{docId}/view")
    public ApiResponse<String> getViewUrl(
            @PathVariable("id") Long id,
            @PathVariable("docId") Long docId,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        
        return ApiResponse.<String>builder()
                .code(200)
                .message("Lấy link xem tài liệu thành công")
                .result(caseService.getViewUrl(id, docId, currentUserId))
                .build();
    }

    @PostMapping("/{id}/documents")
    public ApiResponse<String> uploadDocument(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        String role = getCurrentUserRole(httpRequest);
        requireLawyerRole(role);

        return ApiResponse.<String>builder()
                .code(200)
                .message("Tải tài liệu lên thành công")
                .result(caseService.uploadCaseDocument(id, currentUserId, file))
                .build();
    }

    @DeleteMapping("/{id}/documents/{docId}")
    public ApiResponse<Void> deleteDocument(
            @PathVariable("id") Long id,
            @PathVariable("docId") Long docId,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        String role = getCurrentUserRole(httpRequest);
        requireLawyerRole(role);

        caseService.deleteDocument(id, docId, currentUserId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa tài liệu thành công")
                .build();
    }

    @PostMapping("/{caseId}/progress")
    public ApiResponse<CaseUpdateResponse> updateProgress(
            @PathVariable("caseId") Long caseId,
            @RequestBody UpdateProgressRequest request,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        String role = getCurrentUserRole(httpRequest);
        requireLawyerRole(role);

        return ApiResponse.<CaseUpdateResponse>builder()
                .code(200)
                .message("Cập nhật tiến độ vụ án thành công")
                .result(caseService.updateProgress(caseId, request, currentUserId))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCase(
            @PathVariable("id") Long id,
            HttpServletRequest httpRequest) {
        
        Long currentUserId = getCurrentUserId(httpRequest);
        String role = getCurrentUserRole(httpRequest);
        requireLawyerRole(role);

        caseService.deleteCase(id, currentUserId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa vụ án thành công")
                .build();
    }
}