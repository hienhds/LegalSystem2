package com.example.caseservice.client;

import com.example.caseservice.config.FeignConfig;
import com.example.caseservice.dto.ApiResponse;
import com.example.caseservice.dto.CaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "search-service", 
    url = "${search-service.url:http://search-service}",
    configuration = FeignConfig.class
)
public interface SearchClient {
    @GetMapping("/api/search/cases")
    ApiResponse<Page<CaseResponse>> search(
        @RequestParam("userId") Long userId,
        @RequestParam("role") String role,
        @RequestParam("keyword") String keyword,
        @RequestParam("page") int page,
        @RequestParam("size") int size
    );
}