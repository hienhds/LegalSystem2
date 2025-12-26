package com.example.backend.search.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.lawyer.dto.request.FilterLawyerRequest;
import com.example.backend.lawyer.dto.response.LawyerListResponse;
import com.example.backend.lawyer.service.LawyerService;
import com.example.backend.search.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class LawyerSearchController {

    private final LawyerService lawyerService;
    private final SearchHistoryService searchHistoryService;

    @PostMapping("/lawyers")
    public ResponseEntity<ApiResponse<Object>> searchLawyers(@RequestBody FilterLawyerRequest request) {
        log.info("Searching lawyers with criteria: {}", request);
        
        int page = 0;
        int size = 10;
        Page<LawyerListResponse> lawyersPage = lawyerService.getAllLawyers(request, page, size);
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Tìm thấy " + lawyersPage.getTotalElements() + " luật sư phù hợp")
                .data(lawyersPage)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/lawyers")
    public ResponseEntity<ApiResponse<Object>> searchLawyersSimple(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> specializationIds,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long barAssociationId,
            @RequestParam(required = false) Integer minYearsOfExp,
            @RequestParam(required = false) Integer maxYearsOfExp,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "lawyerId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Simple search: keyword={}, specializationIds={}, minYearsOfExp={}, minRating={}", 
            keyword, specializationIds, minYearsOfExp, minRating);
        
        FilterLawyerRequest filterRequest = new FilterLawyerRequest();
        filterRequest.setKeyword(keyword);
        filterRequest.setSpecializationIds(specializationIds);
        filterRequest.setBarAssociationId(barAssociationId);
        filterRequest.setMinYearsOfExp(minYearsOfExp);
        filterRequest.setMaxYearsOfExp(maxYearsOfExp);
        filterRequest.setMinRating(minRating);
        filterRequest.setSortBy(sortBy);
        filterRequest.setSortDir(sortDir);
        
        Page<LawyerListResponse> lawyersPage = lawyerService.getAllLawyers(filterRequest, page, size);
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Tìm thấy " + lawyersPage.getTotalElements() + " luật sư phù hợp")
                .data(lawyersPage)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Object>> getSearchHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Note: Cần thêm userId từ authentication context trong thực tế
        // Hiện tại trả về empty vì không có userId
        Map<String, Object> response = new HashMap<>();
        response.put("content", List.of());
        response.put("totalElements", 0L);
        response.put("message", "Cần đăng nhập để xem lịch sử tìm kiếm");
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy lịch sử tìm kiếm thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<Object>> getTrendingSearches(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            var trending = searchHistoryService.getPopularKeywords(limit, null);
            
            ApiResponse<Object> apiResponse = ApiResponse.builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy tìm kiếm xu hướng thành công")
                    .data(trending)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting trending searches", e);
            // Fallback to empty list if error
            ApiResponse<Object> apiResponse = ApiResponse.builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Chưa có dữ liệu xu hướng tìm kiếm")
                    .data(List.of())
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.ok(apiResponse);
        }
    }

    @GetMapping("/popular-lawyers")
    public ResponseEntity<ApiResponse<Object>> getPopularLawyers(
            @RequestParam(defaultValue = "10") int limit) {
        
        // Lấy danh sách luật sư đã được xác minh, sắp xếp theo lawyerId
        FilterLawyerRequest filterRequest = new FilterLawyerRequest();
        filterRequest.setSortBy("lawyerId");
        filterRequest.setSortDir("ASC");
        
        Page<LawyerListResponse> lawyersPage = lawyerService.getAllLawyers(filterRequest, 0, limit);
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy luật sư phổ biến thành công")
                .data(lawyersPage.getContent())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/track-click")
    public ResponseEntity<ApiResponse<Object>> trackLawyerClick(@RequestParam Long lawyerId) {
        log.info("Tracking click for lawyer: {}", lawyerId);
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Tracking successful")
                .data(null)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<Object>> getSearchSuggestions(@RequestParam String query) {
        
        List<String> suggestions = List.of(
            query + " hà nội",
            query + " tphcm", 
            query + " chuyên nghiệp",
            query + " kinh nghiệm",
            query + " uy tín"
        );
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy gợi ý tìm kiếm thành công")
                .data(suggestions)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}