package com.example.documentservice.controller;

import com.example.documentservice.dto.ApiResponse;
import com.example.documentservice.dto.DieuRedirectResponse;
import com.example.documentservice.middleware.UserPrincipal;
import com.example.documentservice.service.ChiDanResolveService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/documents/chi-dan")
@RequiredArgsConstructor
public class ChiDanController {

    private final ChiDanResolveService chiDanResolveService;

    @GetMapping("/resolve")
    public ResponseEntity<ApiResponse<DieuRedirectResponse>> resolve(
            @RequestParam String text,
            HttpServletRequest request,
            Authentication authentication
    ) {
        DieuRedirectResponse result = chiDanResolveService.resolve(text);

        ApiResponse<DieuRedirectResponse> response =
                ApiResponse.<DieuRedirectResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Resolve chi dan success")
                        .data(result)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
}
