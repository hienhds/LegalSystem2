package com.example.userservice.lawyer.controller;

import com.example.userservice.common.dto.ApiResponse;
import com.example.userservice.lawyer.dto.response.SpecializationResponse;
import com.example.userservice.lawyer.service.SpecializationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
public class SpecializationController {

    private final SpecializationService specializationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SpecializationResponse>>> getAll(HttpServletRequest request) {

        List<SpecializationResponse> specializationList = specializationService.getAll();

        ApiResponse<List<SpecializationResponse>> response = ApiResponse.<List<SpecializationResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách chuyên môn thành công")
                .data(specializationList)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}