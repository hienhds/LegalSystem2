package com.example.userservice.appointment.controller;

import com.example.userservice.appointment.dto.AvailableSlotResponse;
import com.example.userservice.appointment.dto.BulkAvailabilityRequest;
import com.example.userservice.appointment.dto.LawyerAvailabilityRequest;
import com.example.userservice.appointment.dto.LawyerAvailabilityResponse;
import com.example.userservice.appointment.service.LawyerScheduleService;
import com.example.userservice.common.dto.ApiResponse;
import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.common.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lawyer-schedule")
@RequiredArgsConstructor
@Slf4j
public class LawyerScheduleController {
    
    private final LawyerScheduleService scheduleService;
    
    /**
     * Tạo khung giờ làm việc mới
     * POST /api/lawyer-schedule/availability
     */
    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<LawyerAvailabilityResponse>> createAvailability(
            @Valid @RequestBody LawyerAvailabilityRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        
        // Get lawyerId from authenticated user
        Long lawyerId = user.getLawyerId();
        if (lawyerId == null) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không phải là luật sư");
        }
        
        log.info("Creating availability for lawyer: {}", lawyerId);
        
        LawyerAvailabilityResponse response = scheduleService.createAvailability(request, lawyerId);
        
        ApiResponse<LawyerAvailabilityResponse> apiResponse = ApiResponse.<LawyerAvailabilityResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Tạo khung giờ làm việc thành công")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
    
    /**
     * Tạo nhiều khung giờ làm việc cùng lúc (bulk create)
     * POST /api/lawyer-schedule/availability/bulk
     */
    @PostMapping("/availability/bulk")
    public ResponseEntity<ApiResponse<List<LawyerAvailabilityResponse>>> createBulkAvailability(
            @Valid @RequestBody BulkAvailabilityRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        
        // Get lawyerId from authenticated user
        Long lawyerId = user.getLawyerId();
        if (lawyerId == null) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không phải là luật sư");
        }
        
        log.info("Creating bulk availability for lawyer: {}", lawyerId);
        
        List<LawyerAvailabilityResponse> responses = scheduleService.createBulkAvailability(request, lawyerId);
        
        ApiResponse<List<LawyerAvailabilityResponse>> apiResponse = ApiResponse.<List<LawyerAvailabilityResponse>>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Tạo " + responses.size() + " khung giờ làm việc thành công")
                .data(responses)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
    
    /**
     * Cập nhật khung giờ làm việc
     * PUT /api/lawyer-schedule/availability/{availabilityId}
     */
    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<LawyerAvailabilityResponse>> updateAvailability(
            @PathVariable Long availabilityId,
            @Valid @RequestBody LawyerAvailabilityRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        
        // Get lawyerId from authenticated user
        Long lawyerId = user.getLawyerId();
        if (lawyerId == null) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không phải là luật sư");
        }
        
        log.info("Updating availability {}", availabilityId);
        
        LawyerAvailabilityResponse response = scheduleService.updateAvailability(availabilityId, request, lawyerId);
        
        ApiResponse<LawyerAvailabilityResponse> apiResponse = ApiResponse.<LawyerAvailabilityResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Cập nhật khung giờ làm việc thành công")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Xóa khung giờ làm việc
     * DELETE /api/lawyer-schedule/availability/{availabilityId}
     */
    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(
            @PathVariable Long availabilityId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        
        log.info("Deleting availability: {}", availabilityId);
        
        scheduleService.deleteAvailability(availabilityId);
        
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Xóa khung giờ làm việc thành công")
                .data(null)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Lấy tất cả khung giờ làm việc của luật sư
     * GET /api/lawyer-schedule/availability/lawyer/{lawyerId}
     */
    @GetMapping("/availability/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<List<LawyerAvailabilityResponse>>> getLawyerAvailabilities(
            @PathVariable Long lawyerId,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            HttpServletRequest httpRequest) {
        
        log.info("Getting availabilities for lawyer: {}", lawyerId);
        
        List<LawyerAvailabilityResponse> availabilities = scheduleService.getLawyerAvailabilities(lawyerId, activeOnly);
        
        ApiResponse<List<LawyerAvailabilityResponse>> apiResponse = ApiResponse.<List<LawyerAvailabilityResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách khung giờ làm việc thành công")
                .data(availabilities)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Lấy khung giờ trống của luật sư trong ngày cụ thể
     * GET /api/lawyer-schedule/available-slots/lawyer/{lawyerId}
     */
    @GetMapping("/available-slots/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<AvailableSlotResponse>> getAvailableSlots(
            @PathVariable Long lawyerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "60") Integer durationMinutes,
            HttpServletRequest httpRequest) {
        
        log.info("Getting available slots for lawyer {} on {}", lawyerId, date);
        
        AvailableSlotResponse response = scheduleService.getAvailableSlots(lawyerId, date, durationMinutes);
        
        ApiResponse<AvailableSlotResponse> apiResponse = ApiResponse.<AvailableSlotResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy khung giờ trống thành công")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    
    /**
     * Lấy danh sách khung giờ làm việc của luật sư hiện tại (authenticated)
     * GET /api/lawyer-schedule/my-availability
     */
    @GetMapping("/my-availability")
    public ResponseEntity<ApiResponse<List<LawyerAvailabilityResponse>>> getMyAvailabilities(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            HttpServletRequest httpRequest) {
        
        // TODO: Get lawyerId from authenticated user
        Long lawyerId = user.getUser().getUserId(); // Placeholder
        
        log.info("Getting my availabilities for lawyer: {}", lawyerId);
        
        List<LawyerAvailabilityResponse> availabilities = scheduleService.getLawyerAvailabilities(lawyerId, activeOnly);
        
        ApiResponse<List<LawyerAvailabilityResponse>> apiResponse = ApiResponse.<List<LawyerAvailabilityResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy lịch làm việc của tôi thành công")
                .data(availabilities)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}
