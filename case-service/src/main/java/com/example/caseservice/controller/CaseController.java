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

    // TẠO MỚI
    @PostMapping
    public ApiResponse<CaseResponse> createCase(@RequestBody CreateCaseRequest request,
                                                @RequestHeader("X-User-Id") Long lawyerId) {
        return ApiResponse.<CaseResponse>builder()
                .code(200)
                .result(caseService.createCase(request, lawyerId))
                .build();
    }

    // DANH SÁCH + TÌM KIẾM + PHÂN TRANG
    @GetMapping
    public ApiResponse<Page<CaseResponse>> getMyCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestHeader("X-User-Id") Long userId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.<Page<CaseResponse>>builder()
                .code(200)
                .result(caseService.searchMyCases(userId, keyword, pageable))
                .build();
    }

    // CHI TIẾT
    @GetMapping("/{id}")
    public ApiResponse<CaseResponse> getCaseById(@PathVariable Long id) {
        return ApiResponse.<CaseResponse>builder()
                .code(200)
                .result(caseService.getCaseById(id))
                .build();
    }

    // TIẾN ĐỘ
    @PostMapping("/{caseId}/progress")
    public ApiResponse<CaseUpdateResponse> updateProgress(
            @PathVariable Long caseId,
            @RequestBody UpdateProgressRequest request,
            @RequestHeader("X-User-Id") Long lawyerId) {
        return ApiResponse.<CaseUpdateResponse>builder()
                .code(200)
                .result(caseService.updateProgress(caseId, request, lawyerId))
                .build();
    }

    // UPLOAD TÀI LIỆU
    @PostMapping("/{id}/documents")
    public ApiResponse<String> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.<String>builder()
                .code(200)
                .result(caseService.uploadCaseDocument(id, userId, file))
                .build();
    }

    // XÓA TÀI LIỆU (Dòng 83 bạn báo lỗi nằm ở đây)
    @DeleteMapping("/{id}/documents/{docId}")
    public ApiResponse<Void> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long docId,
            @RequestHeader("X-User-Id") Long userId) {
        caseService.deleteDocument(id, docId, userId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa tài liệu thành công")
                .build();
    }

    // XÓA VỤ ÁN
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCase(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        caseService.deleteCase(id, userId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xóa vụ án thành công")
                .build();
    }
}