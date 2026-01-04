package com.example.documentservice.controller;

import com.example.documentservice.dto.ApiResponse;
import com.example.documentservice.dto.SearchResultResponse;
import com.example.documentservice.service.SearchService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SearchResultResponse>>> searchDocuments(
            @RequestParam String keyword,
            HttpServletRequest request
    ) {
        List<SearchResultResponse> results = searchService.searchFullText(keyword);

        ApiResponse<List<SearchResultResponse>> response = ApiResponse.<List<SearchResultResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(results.isEmpty() ? "No results found" : "Search completed successfully")
                .data(results)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
