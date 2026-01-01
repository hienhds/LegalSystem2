package com.example.documentservice.controller;
import com.example.documentservice.dto.ApiResponse;
import com.example.documentservice.dto.ChuongResponse;
import com.example.documentservice.dto.DieuResponse;
import com.example.documentservice.service.ChuongService;
import com.example.documentservice.service.DieuService;
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
public class DieuController {
    private final DieuService dieuService;

    @GetMapping("/chuong/{chuongId}")
    public ResponseEntity<ApiResponse<List<DieuResponse>>> getAllDieuByChuongId(
            @PathVariable String chuongId,
            HttpServletRequest request
    ) {
        List<DieuResponse> dieuResponses = dieuService.getAllDieuByChuongId(chuongId);

        ApiResponse<List<DieuResponse>> response = ApiResponse.<List<DieuResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Get list success")
                .data(dieuResponses)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
