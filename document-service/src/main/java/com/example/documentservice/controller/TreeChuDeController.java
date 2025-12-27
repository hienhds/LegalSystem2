package com.example.documentservice.controller;

import com.example.documentservice.dto.ApiResponse;
import com.example.documentservice.dto.TreeChuDeContentResponse;
import com.example.documentservice.service.TreeChuDeContentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/documents/tree")
@RequiredArgsConstructor
public class TreeChuDeController {

    private final TreeChuDeContentService service;

    @GetMapping("/chu-de/{chuDeId}")
    public ResponseEntity<ApiResponse<TreeChuDeContentResponse>> loadChuDeContent(
            @PathVariable String chuDeId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request
    ) {
        TreeChuDeContentResponse data =
                service.loadChuDeContent(chuDeId, cursor, limit);

        return ResponseEntity.ok(
                ApiResponse.<TreeChuDeContentResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Load chu-de content success")
                        .data(data)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }
}
