package com.example.caseservice.controller;

import com.example.caseservice.dto.*;
import com.example.caseservice.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {
    private final CaseService caseService;

    @PostMapping
    public ApiResponse<CaseResponse> createCase(
            @RequestBody CreateCaseRequest request,
            @RequestHeader("X-User-Id") Long lawyerId,
            @RequestHeader("X-User-Role") String role) {
        if (!"LAWYER".equalsIgnoreCase(role)) {
            throw new RuntimeException("Chỉ luật sư mới có quyền tạo vụ án");
        }
        return ApiResponse.<CaseResponse>builder()
                .code(200)
                .message("Tạo vụ án mới thành công")
                .result(caseService.createCase(request, lawyerId))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<CaseResponse>> getMyCases(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.<Page<CaseResponse>>builder()
                .code(200)
                .message("Lấy danh sách vụ án thành công")
                .result(caseService.searchMyCases(userId, role, keyword, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CaseResponse> getCaseById(@PathVariable("id") Long id) {
        return ApiResponse.<CaseResponse>builder()
                .code(200)
                .message("Lấy thông tin chi tiết vụ án thành công")
                .result(caseService.getCaseById(id))
                .build();
    }

    @GetMapping("/{id}/documents/{docId}/download")
    public ApiResponse<String> getDownloadUrl(
            @PathVariable("id") Long id,
            @PathVariable("docId") Long docId,
            @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Lấy link tải tài liệu thành công")
                .result(caseService.getDownloadUrl(id, docId, userId))
                .build();
    }

    @GetMapping("/{id}/documents/{docId}/view")
    public ApiResponse<String> getViewUrl(
            @PathVariable("id") Long id,
            @PathVariable("docId") Long docId,
            @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Lấy link xem tài liệu thành công")
                .result(caseService.getViewUrl(id, docId, userId))
                .build();
    }

    @PostMapping("/{id}/documents")
    public ApiResponse<String> uploadDocument(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Tải tài liệu lên thành công")
                .result(caseService.uploadCaseDocument(id, userId, file))
                .build();
    }

    @DeleteMapping("/{id}/documents/{docId}")
    public ApiResponse<Void> deleteDocument(
            @PathVariable("id") Long id,
            @PathVariable("docId") Long docId,
            @RequestHeader("X-User-Id") Long userId) {
        caseService.deleteDocument(id, docId, userId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa tài liệu thành công")
                .build();
    }

    @PostMapping("/{caseId}/progress")
    public ApiResponse<CaseUpdateResponse> updateProgress(
            @PathVariable("caseId") Long caseId,
            @RequestBody UpdateProgressRequest request,
            @RequestHeader("X-User-Id") Long lawyerId) {
        return ApiResponse.<CaseUpdateResponse>builder()
                .code(200)
                .message("Cập nhật tiến độ vụ án thành công")
                .result(caseService.updateProgress(caseId, request, lawyerId))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCase(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId) {
        caseService.deleteCase(id, userId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa vụ án thành công")
                .build();
    }
}