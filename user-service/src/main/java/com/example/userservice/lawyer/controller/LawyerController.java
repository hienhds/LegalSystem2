package com.example.userservice.lawyer.controller;

import com.example.userservice.common.dto.ApiResponse;
import com.example.userservice.common.security.CustomUserDetails;
import com.example.userservice.common.service.UploadImageService;
import com.example.userservice.lawyer.dto.request.LawyerRequest;
import com.example.userservice.lawyer.dto.response.LawyerResponse;
import com.example.userservice.lawyer.service.LawyerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("/api/lawyers")
@RequiredArgsConstructor
public class LawyerController {

    private final LawyerService lawyerService;
    private final UploadImageService uploadImageService;
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

}