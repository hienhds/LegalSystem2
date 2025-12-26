package com.example.backend.lawyer.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.lawyer.entity.Specialization;
import com.example.backend.lawyer.service.SpecializationService;
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
@RequestMapping("/api/specialization")
@RequiredArgsConstructor
public class SpecializationController {

    private final SpecializationService specializationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Specialization>>> getAll(HttpServletRequest request) {

        List<Specialization> specializationList = specializationService.getAll();

        ApiResponse<List<Specialization>> response = ApiResponse.<List<Specialization>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Thành công")
                .data(specializationList)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}