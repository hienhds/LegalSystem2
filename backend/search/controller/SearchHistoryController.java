package com.example.backend.search.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.security.CustomUserDetails;
import com.example.backend.search.dto.*;
import com.example.backend.search.service.SearchHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    /**
     * Lấy lịch sử tìm kiếm của user hiện tại
     */
    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('LAWYER') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SearchHistoryListResponse>> getUserSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        
        try {
            Long userId = userDetails.getUser().getUserId();
            log.debug("Getting search history for user: {} with page: {}, size: {}", 
                     userId, page, size);
            
            SearchHistoryListResponse result = searchHistoryService.getUserSearchHistory(
                    userId, page, size);
            
            ApiResponse<SearchHistoryListResponse> response = ApiResponse.<SearchHistoryListResponse>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy lịch sử tìm kiếm thành công")
                    .data(result)
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Long userId = userDetails.getUser().getUserId();
            log.error("Error getting search history for user: {} - {}", 
                     userId, e.getMessage(), e);
            
            ApiResponse<SearchHistoryListResponse> response = ApiResponse.<SearchHistoryListResponse>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi lấy lịch sử tìm kiếm: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy lịch sử tìm kiếm của user cụ thể (ADMIN only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SearchHistoryListResponse>> getUserSearchHistoryByAdmin(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        
        try {
            log.debug("Admin getting search history for user: {} with page: {}, size: {}", 
                     userId, page, size);
            
            SearchHistoryListResponse result = searchHistoryService.getUserSearchHistory(
                    userId, page, size);
            
            ApiResponse<SearchHistoryListResponse> response = ApiResponse.<SearchHistoryListResponse>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy lịch sử tìm kiếm thành công")
                    .data(result)
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting search history for user: {} - {}", 
                     userId, e.getMessage(), e);
            
            ApiResponse<SearchHistoryListResponse> response = ApiResponse.<SearchHistoryListResponse>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi lấy lịch sử tìm kiếm: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy lịch sử tìm kiếm với filters nâng cao
     */
    @PostMapping("/history/filter")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('LAWYER') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SearchHistoryListResponse>> getFilteredSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SearchHistoryFilterRequest filterRequest,
            HttpServletRequest request) {
        
        try {
            Long userId = userDetails.getUser().getUserId();
            log.debug("Getting filtered search history for user: {} with filters: {}", 
                     userId, filterRequest);
            
            // Validate filter request
            if (!filterRequest.isDateRangeValid() || !filterRequest.isResultCountRangeValid()) {
                ApiResponse<SearchHistoryListResponse> response = ApiResponse.<SearchHistoryListResponse>builder()
                        .success(false)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Filters không hợp lệ")
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build();
                return ResponseEntity.badRequest().body(response);
            }
            
            SearchHistoryListResponse result = searchHistoryService.getUserSearchHistoryFiltered(
                    userId, filterRequest);
            
            ApiResponse<SearchHistoryListResponse> response = ApiResponse.<SearchHistoryListResponse>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy lịch sử tìm kiếm với filters thành công")
                    .data(result)
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Long userId = userDetails.getUser().getUserId();
            log.error("Error getting filtered search history for user: {} - {}", 
                     userId, e.getMessage(), e);
            
            ApiResponse<SearchHistoryListResponse> response = ApiResponse.<SearchHistoryListResponse>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi lấy lịch sử tìm kiếm: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lưu lịch sử tìm kiếm mới
     */
    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('LAWYER') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SearchHistoryResponse>> saveSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SearchHistoryRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = userDetails.getUser().getUserId();
            log.debug("Saving search history for user: {} with keyword: {}", 
                     userId, request.getSearchKeyword());
            
            SearchHistoryResponse result = searchHistoryService.saveSearchHistory(userId, request);
            
            ApiResponse<SearchHistoryResponse> response = ApiResponse.<SearchHistoryResponse>builder()
                    .success(true)
                    .status(HttpStatus.CREATED.value())
                    .message("Lưu lịch sử tìm kiếm thành công")
                    .data(result)
                    .timestamp(Instant.now())
                    .path(httpRequest.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            Long userId = userDetails.getUser().getUserId();
            log.warn("Invalid search history request from user: {} - {}", userId, e.getMessage());
            
            ApiResponse<SearchHistoryResponse> response = ApiResponse.<SearchHistoryResponse>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Dữ liệu không hợp lệ: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(httpRequest.getRequestURI())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Long userId = userDetails.getUser().getUserId();
            log.error("Error saving search history for user: {} - {}", userId, e.getMessage(), e);
            
            ApiResponse<SearchHistoryResponse> response = ApiResponse.<SearchHistoryResponse>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi lưu lịch sử tìm kiếm: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(httpRequest.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa toàn bộ lịch sử tìm kiếm của user
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('LAWYER') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<DeleteHistoryResponse>> clearUserHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        
        try {
            Long userId = userDetails.getUser().getUserId();
            log.debug("Clearing search history for user: {}", userId);
            
            DeleteHistoryResponse result = searchHistoryService.clearUserHistory(userId);
            
            ApiResponse<DeleteHistoryResponse> response = ApiResponse.<DeleteHistoryResponse>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Xóa lịch sử tìm kiếm thành công")
                    .data(result)
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Long userId = userDetails.getUser().getUserId();
            log.error("Error clearing search history for user: {} - {}", userId, e.getMessage(), e);
            
            ApiResponse<DeleteHistoryResponse> response = ApiResponse.<DeleteHistoryResponse>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi xóa lịch sử tìm kiếm: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy từ khóa phổ biến
     */
    @GetMapping("/popular-keywords")
    public ResponseEntity<ApiResponse<PopularKeywordsResponse>> getPopularKeywords(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "all_time") String period,
            HttpServletRequest request) {
        
        try {
            log.debug("Getting popular keywords with limit: {} and period: {}", limit, period);
            
            if (limit <= 0 || limit > 100) {
                ApiResponse<PopularKeywordsResponse> response = ApiResponse.<PopularKeywordsResponse>builder()
                        .success(false)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Limit phải từ 1 đến 100")
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build();
                return ResponseEntity.badRequest().body(response);
            }
            
            PopularKeywordsResponse result = searchHistoryService.getPopularKeywords(limit, period);
            
            ApiResponse<PopularKeywordsResponse> response = ApiResponse.<PopularKeywordsResponse>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy từ khóa phổ biến thành công")
                    .data(result)
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting popular keywords - {}", e.getMessage(), e);
            
            ApiResponse<PopularKeywordsResponse> response = ApiResponse.<PopularKeywordsResponse>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi lấy từ khóa phổ biến: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thống kê tìm kiếm của user hiện tại
     */
    @GetMapping("/user-statistics")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('LAWYER') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SearchStatisticsResponse>> getUserSearchStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        
        try {
            Long userId = userDetails.getUser().getUserId();
            log.debug("Getting search statistics for user: {}", userId);
            
            SearchStatisticsResponse result = searchHistoryService.getUserSearchStatistics(userId);
            
            ApiResponse<SearchStatisticsResponse> response = ApiResponse.<SearchStatisticsResponse>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy thống kê tìm kiếm cá nhân thành công")
                    .data(result)
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Long userId = userDetails.getUser().getUserId();
            log.error("Error getting user search statistics for user: {} - {}", userId, e.getMessage(), e);
            
            ApiResponse<SearchStatisticsResponse> response = ApiResponse.<SearchStatisticsResponse>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi lấy thống kê tìm kiếm: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy gợi ý từ khóa cho user
     */
    @GetMapping("/history-suggestions")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('LAWYER') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        
        try {
            Long userId = userDetails.getUser().getUserId();
            log.debug("Getting search suggestions for user: {} with keyword: {}", userId, keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                ApiResponse<List<String>> response = ApiResponse.<List<String>>builder()
                        .success(false)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Keyword không được để trống")
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build();
                return ResponseEntity.badRequest().body(response);
            }
            
            if (limit <= 0 || limit > 20) {
                ApiResponse<List<String>> response = ApiResponse.<List<String>>builder()
                        .success(false)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Limit phải từ 1 đến 20")
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build();
                return ResponseEntity.badRequest().body(response);
            }
            
            List<String> suggestions = searchHistoryService.getSearchSuggestions(userId, keyword, limit);
            
            ApiResponse<List<String>> response = ApiResponse.<List<String>>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy gợi ý từ khóa thành công")
                    .data(suggestions)
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Long userId = userDetails.getUser().getUserId();
            log.error("Error getting search suggestions for user: {} - {}", userId, e.getMessage(), e);
            
            ApiResponse<List<String>> response = ApiResponse.<List<String>>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi lấy gợi ý từ khóa: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa lịch sử tìm kiếm cụ thể
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('LAWYER') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            HttpServletRequest request) {
        
        try {
            Long userId = userDetails.getUser().getUserId();
            log.debug("Deleting search history {} for user: {}", id, userId);
            
            searchHistoryService.deleteUserSearchHistory(userId, id);
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Xóa lịch sử tìm kiếm thành công")
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Long userId = userDetails.getUser().getUserId();
            log.error("Error deleting search history {} for user: {} - {}", id, userId, e.getMessage(), e);
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi xóa lịch sử tìm kiếm: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thống kê tìm kiếm toàn hệ thống  
     */
    @GetMapping("/system-statistics")
    public ResponseEntity<ApiResponse<SearchStatisticsResponse>> getSystemStatistics(
            @RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {
        
        try {
            log.debug("Getting system search statistics for {} days", days);
            
            SearchStatisticsResponse result = searchHistoryService.getSystemSearchStatistics(days);
            
            ApiResponse<SearchStatisticsResponse> response = ApiResponse.<SearchStatisticsResponse>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy thống kê hệ thống thành công")
                    .data(result)
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting system statistics - {}", e.getMessage(), e);
            
            ApiResponse<SearchStatisticsResponse> response = ApiResponse.<SearchStatisticsResponse>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi khi lấy thống kê hệ thống: " + e.getMessage())
                    .timestamp(Instant.now())
                    .path(request.getRequestURI())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}