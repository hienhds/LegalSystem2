package com.example.documentservice.controller;

import com.example.documentservice.dto.ApiResponse;
import com.example.documentservice.dto.ChuongResponse;
import com.example.documentservice.service.ChuongService;
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
public class ChuongController {

    private final ChuongService chuongService;

    @GetMapping("/de-muc/{deMucId}")
    public ResponseEntity<ApiResponse<List<ChuongResponse>>> getChuongByDeMucId(
            @PathVariable String deMucId,
            HttpServletRequest request
    ) {
        List<ChuongResponse> chuongResponses = chuongService.getChuongByDeMucId(deMucId);

        ApiResponse<List<ChuongResponse>> response = ApiResponse.<List<ChuongResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Get list success")
                .data(chuongResponses)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
