package com.example.backend.lawyer.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.lawyer.dto.response.BarAssociationResponse;
import com.example.backend.lawyer.service.BarAssociationService;
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
@RequestMapping("/api/bar-association")
@RequiredArgsConstructor
public class BarAssociationController {

    private final BarAssociationService barAssociationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BarAssociationResponse>>> getAll(HttpServletRequest servletRequest) {

        List<BarAssociationResponse> allBarAssociation = barAssociationService.getAllBarAssociation();

        ApiResponse<List<BarAssociationResponse>> response = ApiResponse.<List<BarAssociationResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Thành công")
                .data(allBarAssociation)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
