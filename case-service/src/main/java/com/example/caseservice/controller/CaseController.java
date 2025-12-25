package com.example.caseservice.controller;

import com.example.caseservice.dto.ApiResponse;
import com.example.caseservice.dto.CaseResponse;
import com.example.caseservice.dto.CreateCaseRequest;
import com.example.caseservice.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.caseservice.dto.CaseUpdateResponse;    // THÊM DÒNG NÀY
import com.example.caseservice.dto.UpdateProgressRequest; //
import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {
    private final CaseService caseService;

    @PostMapping
    public ApiResponse<CaseResponse> createCase(@RequestBody CreateCaseRequest request,
                                                @RequestHeader("X-User-Id") Long lawyerId) {
        return ApiResponse.<CaseResponse>builder()
                .code(200)
                .result(caseService.createCase(request, lawyerId))
                .build();
    }

    @GetMapping("/my-cases")
    public ApiResponse<List<CaseResponse>> getMyCases(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.<List<CaseResponse>>builder()
                .code(200)
                .result(caseService.getMyCases(userId))
                .build();
    }
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
}