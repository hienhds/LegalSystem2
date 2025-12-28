package com.example.documentservice.controller;

import com.example.documentservice.dto.ApiResponse;
import com.example.documentservice.dto.DeMucResponse;
import com.example.documentservice.service.DeMucService;
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
public class DeMucController {

    private final DeMucService deMucService;

    @GetMapping("/chu-de/{chuDeId}")
    public ResponseEntity<ApiResponse<List<DeMucResponse>>> getDeMucByChuDeId(
            @PathVariable String chuDeId,
            HttpServletRequest request
    ) {
        List<DeMucResponse> deMucResponses = deMucService.getDeMucByChuDeId(chuDeId);

        ApiResponse<List<DeMucResponse>> response = ApiResponse.<List<DeMucResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Get list success")
                .data(deMucResponses)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
