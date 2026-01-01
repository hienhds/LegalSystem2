package com.example.documentservice.controller;

import com.example.documentservice.dto.ApiResponse;
import com.example.documentservice.dto.ChuDeResponse;
import com.example.documentservice.service.ChuDeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents/chu-de")
@RequiredArgsConstructor
public class ChuDeController {

    private final ChuDeService chuDeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChuDeResponse>>> getAllChuDe(HttpServletRequest request){
        List<ChuDeResponse> chuDeResponseList = chuDeService.getAllChuDe();

        ApiResponse<List<ChuDeResponse>> response = ApiResponse.<List<ChuDeResponse>>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Get list success")
                .data(chuDeResponseList)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
