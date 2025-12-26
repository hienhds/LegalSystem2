package com.example.backend.document.controller;

import com.example.backend.document.dto.CreateDocumentRequest;
import com.example.backend.document.dto.DocumentCategoryResponse;
import com.example.backend.document.dto.DocumentSearchRequest;
import com.example.backend.document.dto.LegalDocumentResponse;
import com.example.backend.document.entity.LegalDocument;
import com.example.backend.document.service.LegalDocumentService;
import com.example.backend.document.service.DocumentViewTracker;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.security.CustomUserDetails;
import com.example.backend.common.util.IpAddressUtil;
import com.example.backend.search.dto.SearchHistoryRequest;
import com.example.backend.search.entity.SearchModule;
import com.example.backend.search.entity.SearchType;
import com.example.backend.search.service.SearchHistoryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;
    private final SearchHistoryService searchHistoryService;
    private final DocumentViewTracker viewTracker;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<LegalDocumentResponse>>> searchDocuments(
            @Valid @ModelAttribute DocumentSearchRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Search request: keyword='{}', category='{}', page={}, size={}, sortBy='{}', sortDirection='{}'", 
                 request.getKeyword(), request.getCategory(), request.getPage(), request.getSize(), 
                 request.getSortBy(), request.getSortDirection());
        
        long startTime = System.currentTimeMillis();
        
        Page<LegalDocument> documents = legalDocumentService.advancedSearch(
                request.getCleanKeyword(),
                request.getCleanCategory(),
                request.getPage(),
                request.getSize(),
                request.getSortBy(),
                request.getSortDirection()
        );
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Track search history for authenticated users
        if (userDetails != null && request.hasKeyword()) {
            try {
                Map<String, Object> filters = new HashMap<>();
                if (request.hasCategory()) {
                    filters.put("category", request.getCleanCategory());
                }
                filters.put("sortBy", request.getSortBy());
                filters.put("sortDirection", request.getSortDirection());
                
                SearchHistoryRequest historyRequest = new SearchHistoryRequest(
                        request.getCleanKeyword(),
                        SearchModule.LEGAL_DOCUMENT,
                        SearchType.ADVANCED,
                        (int) documents.getTotalElements()
                );
                historyRequest.setFilters(filters);
                historyRequest.setExecutionTime(executionTime);
                
                // Save search history asynchronously to not block the main response
                searchHistoryService.saveSearchHistoryAsync(
                        userDetails.getUser().getUserId(), 
                        historyRequest
                );
                
                log.debug("Search history tracked for user: {} with keyword: {}", 
                         userDetails.getUser().getUserId(), request.getCleanKeyword());
                         
            } catch (Exception e) {
                log.warn("Failed to track search history for user: {} - {}", 
                        userDetails.getUser().getUserId(), e.getMessage());
                // Continue with the main response even if history tracking fails
            }
        }
        
        Page<LegalDocumentResponse> responseDocuments = documents.map(LegalDocumentResponse::fromEntity);
        
        ApiResponse<Page<LegalDocumentResponse>> apiResponse = ApiResponse.<Page<LegalDocumentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Tìm kiếm văn bản thành công")
                .data(responseDocuments)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> getDocumentById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        
        log.info("Getting document with ID: {}", id);
        
        long startTime = System.currentTimeMillis();
        
        // Get identifier for view tracking (user ID or IP address)
        String ipAddress = IpAddressUtil.getClientIpAddress(request);
        Long userId = userDetails != null ? userDetails.getUser().getUserId() : null;
        String identifier = viewTracker.getIdentifier(ipAddress, userId);
        
        LegalDocument document = legalDocumentService.getDocumentById(id, identifier);
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Track search history for authenticated users
        if (userDetails != null && document != null) {
            try {
                SearchHistoryRequest historyRequest = new SearchHistoryRequest(
                        "Document ID: " + id,
                        SearchModule.LEGAL_DOCUMENT,
                        SearchType.BY_ID,
                        1 // Found one document
                );
                historyRequest.setExecutionTime(executionTime);
                
                searchHistoryService.saveSearchHistoryAsync(
                        userDetails.getUser().getUserId(), 
                        historyRequest
                );
                
                log.debug("Document access tracked for user: {} with document ID: {}", 
                         userDetails.getUser().getUserId(), id);
                         
            } catch (Exception e) {
                log.warn("Failed to track document access for user: {} - {}", 
                        userDetails.getUser().getUserId(), e.getMessage());
            }
        }
        
        if (document == null) {
            ApiResponse<LegalDocumentResponse> apiResponse = ApiResponse.<LegalDocumentResponse>builder()
                    .success(false)
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Không tìm thấy văn bản pháp luật với ID: " + id)
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
        
        LegalDocumentResponse response = LegalDocumentResponse.fromEntity(document);
        
        ApiResponse<LegalDocumentResponse> apiResponse = ApiResponse.<LegalDocumentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin văn bản thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<LegalDocumentResponse>>> getDocumentsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Getting documents by category: {}, page: {}, size: {}", category, page, size);
        
        long startTime = System.currentTimeMillis();
        
        Page<LegalDocument> documents = legalDocumentService.getDocumentsByCategory(category, page, size);
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Track search history for authenticated users
        if (userDetails != null && category != null && !category.trim().isEmpty()) {
            try {
                Map<String, Object> filters = new HashMap<>();
                filters.put("category", category.trim());
                
                SearchHistoryRequest historyRequest = new SearchHistoryRequest(
                        "Category: " + category.trim(),
                        SearchModule.LEGAL_DOCUMENT,
                        SearchType.CATEGORY,
                        (int) documents.getTotalElements()
                );
                historyRequest.setFilters(filters);
                historyRequest.setExecutionTime(executionTime);
                
                searchHistoryService.saveSearchHistoryAsync(
                        userDetails.getUser().getUserId(), 
                        historyRequest
                );
                
                log.debug("Category search history tracked for user: {} with category: {}", 
                         userDetails.getUser().getUserId(), category);
                         
            } catch (Exception e) {
                log.warn("Failed to track category search history for user: {} - {}", 
                        userDetails.getUser().getUserId(), e.getMessage());
            }
        }
        Page<LegalDocumentResponse> responseDocuments = documents.map(LegalDocumentResponse::fromEntity);
        
        ApiResponse<Page<LegalDocumentResponse>> apiResponse = ApiResponse.<Page<LegalDocumentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(String.format("Lấy danh sách văn bản thuộc danh mục '%s' thành công", category))
                .data(responseDocuments)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<Page<LegalDocumentResponse>>> getTrendingDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Getting trending documents, page: {}, size: {}", page, size);
        
        long startTime = System.currentTimeMillis();
        
        Page<LegalDocument> documents = legalDocumentService.getTrendingDocuments(page, size);
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Track search history for authenticated users
        if (userDetails != null) {
            try {
                SearchHistoryRequest historyRequest = new SearchHistoryRequest(
                        "Trending Documents",
                        SearchModule.LEGAL_DOCUMENT,
                        SearchType.TRENDING,
                        (int) documents.getTotalElements()
                );
                historyRequest.setExecutionTime(executionTime);
                
                searchHistoryService.saveSearchHistoryAsync(
                        userDetails.getUser().getUserId(), 
                        historyRequest
                );
                
                log.debug("Trending search tracked for user: {}", 
                         userDetails.getUser().getUserId());
                         
            } catch (Exception e) {
                log.warn("Failed to track trending search for user: {} - {}", 
                        userDetails.getUser().getUserId(), e.getMessage());
            }
        }
        Page<LegalDocumentResponse> responseDocuments = documents.map(LegalDocumentResponse::fromEntity);
        
        ApiResponse<Page<LegalDocumentResponse>> apiResponse = ApiResponse.<Page<LegalDocumentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách văn bản phổ biến thành công")
                .data(responseDocuments)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<DocumentCategoryResponse.DocumentCategoriesResponse>> getAllCategories() {
        
        log.info("Getting all available categories");
        
        List<String> categories = legalDocumentService.getAllCategories();
        DocumentCategoryResponse.DocumentCategoriesResponse response = 
            DocumentCategoryResponse.DocumentCategoriesResponse.fromCategories(categories);
        
        ApiResponse<DocumentCategoryResponse.DocumentCategoriesResponse> apiResponse = 
            ApiResponse.<DocumentCategoryResponse.DocumentCategoriesResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách danh mục thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/categories/stats")
    public ResponseEntity<ApiResponse<DocumentCategoryResponse.DocumentCategoriesResponse>> getCategoryStats() {
        
        log.info("Getting category statistics");
        
        List<Object[]> rawStats = legalDocumentService.getDocumentCountsByCategory();
        List<DocumentCategoryResponse> categoryStats = rawStats.stream()
                .map(stats -> DocumentCategoryResponse.fromCategoryStats(
                        (String) stats[0], 
                        ((Number) stats[1]).longValue()))
                .collect(Collectors.toList());
        
        DocumentCategoryResponse.DocumentCategoriesResponse response = 
            DocumentCategoryResponse.DocumentCategoriesResponse.fromCategoryStats(categoryStats);
        
        ApiResponse<DocumentCategoryResponse.DocumentCategoriesResponse> apiResponse = 
            ApiResponse.<DocumentCategoryResponse.DocumentCategoriesResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thống kê danh mục thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LegalDocumentResponse>>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Getting all documents, page: {}, size: {}, sortBy: {}, direction: {}", 
                 page, size, sortBy, sortDirection);
        
        Page<LegalDocument> documents = legalDocumentService.getAllDocuments(page, size, sortBy, sortDirection);
        Page<LegalDocumentResponse> responseDocuments = documents.map(LegalDocumentResponse::fromEntity);
        
        ApiResponse<Page<LegalDocumentResponse>> apiResponse = ApiResponse.<Page<LegalDocumentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách văn bản thành công")
                .data(responseDocuments)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/general-search")
    public ResponseEntity<ApiResponse<Page<LegalDocumentResponse>>> generalSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("General search with keyword: '{}', page: {}, size: {}", keyword, page, size);
        
        long startTime = System.currentTimeMillis();
        
        Page<LegalDocument> documents = legalDocumentService.generalSearch(keyword, page, size);
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Track search history for authenticated users with keywords
        if (userDetails != null && keyword != null && !keyword.trim().isEmpty()) {
            try {
                SearchHistoryRequest historyRequest = new SearchHistoryRequest(
                        keyword.trim(),
                        SearchModule.LEGAL_DOCUMENT,
                        SearchType.GENERAL,
                        (int) documents.getTotalElements()
                );
                historyRequest.setExecutionTime(executionTime);
                
                searchHistoryService.saveSearchHistoryAsync(
                        userDetails.getUser().getUserId(), 
                        historyRequest
                );
                
                log.debug("General search history tracked for user: {} with keyword: {}", 
                         userDetails.getUser().getUserId(), keyword);
                         
            } catch (Exception e) {
                log.warn("Failed to track general search history for user: {} - {}", 
                        userDetails.getUser().getUserId(), e.getMessage());
            }
        }
        Page<LegalDocumentResponse> responseDocuments = documents.map(LegalDocumentResponse::fromEntity);
        
        String message = keyword != null && !keyword.trim().isEmpty() 
                ? String.format("Tìm kiếm với từ khóa '%s' thành công", keyword)
                : "Lấy tất cả văn bản thành công";
        
        ApiResponse<Page<LegalDocumentResponse>> apiResponse = ApiResponse.<Page<LegalDocumentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .data(responseDocuments)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'LAWYER')")
    public ResponseEntity<ApiResponse<LegalDocumentResponse>> createDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creating new document by user: {} ({})", 
                 userDetails.getUser().getUserId(), userDetails.getUser().getEmail());
        
        LegalDocument document = legalDocumentService.createDocument(
                request.getTitle(),
                request.getCategory(),
                request.getFileUrl()
        );
        
        LegalDocumentResponse response = LegalDocumentResponse.fromEntity(document);
        
        ApiResponse<LegalDocumentResponse> apiResponse = ApiResponse.<LegalDocumentResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Tạo văn bản pháp luật thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}