package com.example.backend.lawyer.controller;

import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.security.CustomUserDetails;
import com.example.backend.common.service.UploadImageService;
import com.example.backend.lawyer.dto.request.LawyerRequest;
import com.example.backend.lawyer.dto.request.UpdateLawyerProfileRequest;
import com.example.backend.lawyer.dto.response.LawyerDetailResponse;
import com.example.backend.lawyer.dto.response.LawyerListResponse;
import com.example.backend.lawyer.dto.response.LawyerResponse;
import com.example.backend.lawyer.dto.response.LawyerStatsResponse;
import com.example.backend.lawyer.service.LawyerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("/api/lawyers")
@RequiredArgsConstructor
public class LawyerController {

    private final LawyerService lawyerService;
    private final UploadImageService uploadImageService;
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LawyerListResponse>> getLawyerById(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        LawyerListResponse lawyer = lawyerService.getLawyerById(id);
        
        ApiResponse<LawyerListResponse> response = ApiResponse.<LawyerListResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin luật sư thành công")
                .data(lawyer)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<LawyerStatsResponse>> getStats(
            HttpServletRequest request) {
        
        LawyerStatsResponse stats = lawyerService.getStats();
        
        ApiResponse<LawyerStatsResponse> response = ApiResponse.<LawyerStatsResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thống kê luật sư thành công")
                .data(stats)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LawyerResponse>> createLawyer(
            @RequestParam("data") String data,           // <--- String, không phải LawyerRequest
            @RequestParam("certificate") MultipartFile file,
            HttpServletRequest servletRequest,
            @AuthenticationPrincipal CustomUserDetails user
    ) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        LawyerRequest request = mapper.readValue(data, LawyerRequest.class);

        Long userId = user.getUser().getUserId();
        String url = uploadImageService.uploadImage(userId, file, "certificates");
        LawyerResponse lawyerResponse = lawyerService.requestUpgrade(request, userId, url);

        ApiResponse<LawyerResponse> response = ApiResponse.<LawyerResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Dang ky thanh cong cho phe duyet")
                .data(lawyerResponse)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping(value = "/certificates", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> uploadCertificate(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest request) {

        Long userId = user.getUser().getUserId();
        String url = uploadImageService.uploadImage(userId, file, "certificates");

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Upload certificate thành công")
                .data(url)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<LawyerDetailResponse>> updateLawyerProfile(
            @Valid @RequestBody UpdateLawyerProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest servletRequest) {

        Long userId = user.getUser().getUserId();
        LawyerDetailResponse lawyerDetail = lawyerService.updateLawyerProfile(userId, request);

        ApiResponse<LawyerDetailResponse> response = ApiResponse.<LawyerDetailResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Cập nhật thông tin luật sư thành công")
                .data(lawyerDetail)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<com.example.backend.lawyer.dto.response.LawyerReviewResponse>>> getLawyerReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        org.springframework.data.domain.Page<com.example.backend.lawyer.dto.response.LawyerReviewResponse> reviews = 
                lawyerService.getLawyerReviews(id, page, size);
        
        ApiResponse<org.springframework.data.domain.Page<com.example.backend.lawyer.dto.response.LawyerReviewResponse>> response = 
                ApiResponse.<org.springframework.data.domain.Page<com.example.backend.lawyer.dto.response.LawyerReviewResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách đánh giá thành công")
                .data(reviews)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(response);
    }

}