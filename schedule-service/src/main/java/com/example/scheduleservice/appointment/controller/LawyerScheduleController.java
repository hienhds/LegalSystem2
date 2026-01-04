package com.example.scheduleservice.appointment.controller;

import com.example.scheduleservice.appointment.dto.LawyerAvailabilityRequest;
import com.example.scheduleservice.appointment.dto.LawyerAvailabilityUpdateRequest;
import com.example.scheduleservice.appointment.dto.LawyerAvailabilityResponse;
import com.example.scheduleservice.appointment.service.LawyerScheduleService;
import com.example.scheduleservice.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lawyer-schedule")
@RequiredArgsConstructor
@Slf4j
public class LawyerScheduleController {

    private final LawyerScheduleService scheduleService;

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<List<LawyerAvailabilityResponse>>> createAvailability(
            @Valid @RequestBody LawyerAvailabilityRequest request) {
        
        log.info("Creating availability for lawyer {} on days {}", request.getLawyerId(), request.getDaysOfWeek());
        
        try {
            List<LawyerAvailabilityResponse> responses = scheduleService.createAvailability(request);
            
            String message = responses.size() == request.getDaysOfWeek().size()
                    ? "Tạo khung giờ làm việc thành công cho tất cả các ngày"
                    : String.format("Tạo khung giờ làm việc thành công cho %d/%d ngày (một số ngày đã tồn tại)", 
                            responses.size(), request.getDaysOfWeek().size());
            
            ApiResponse<List<LawyerAvailabilityResponse>> apiResponse = ApiResponse.<List<LawyerAvailabilityResponse>>builder()
                    .success(true)
                    .status(HttpStatus.CREATED.value())
                    .message(message)
                    .data(responses)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse<List<LawyerAvailabilityResponse>> apiResponse = ApiResponse.<List<LawyerAvailabilityResponse>>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<LawyerAvailabilityResponse>> updateAvailability(
            @PathVariable Long availabilityId,
            @Valid @RequestBody LawyerAvailabilityUpdateRequest request) {
        
        log.info("Updating availability {}", availabilityId);
        
        try {
            LawyerAvailabilityResponse response = scheduleService.updateAvailability(availabilityId, request);
            
            ApiResponse<LawyerAvailabilityResponse> apiResponse = ApiResponse.<LawyerAvailabilityResponse>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Cập nhật khung giờ làm việc thành công")
                    .data(response)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse<LawyerAvailabilityResponse> apiResponse = ApiResponse.<LawyerAvailabilityResponse>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<Object>> deleteAvailability(
            @PathVariable Long availabilityId) {
        
        // TODO: Get lawyerId from JWT
        Long lawyerId = 7L; // Temporary
        log.info("Deleting availability {}", availabilityId);
        
        try {
            scheduleService.deleteAvailability(availabilityId, lawyerId);
            
            ApiResponse<Object> apiResponse = ApiResponse.builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Xóa khung giờ làm việc thành công")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (IllegalArgumentException e) {
            ApiResponse<Object> apiResponse = ApiResponse.builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<LawyerAvailabilityResponse>>> getMyAvailabilities() {
        
        // TODO: Get lawyerId from JWT token
        Long lawyerId = 7L; // Temporary hardcode
        log.info("Getting availabilities for lawyer {}", lawyerId);
        
        List<LawyerAvailabilityResponse> availabilities = scheduleService.getLawyerAvailabilities(lawyerId);
        
        ApiResponse<List<LawyerAvailabilityResponse>> apiResponse = ApiResponse.<List<LawyerAvailabilityResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách khung giờ làm việc thành công")
                .data(availabilities)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/availability/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<List<LawyerAvailabilityResponse>>> getSpecificLawyerAvailabilities(
            @PathVariable Long lawyerId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        log.info("Getting availabilities for lawyer {} (activeOnly: {})", lawyerId, activeOnly);
        
        List<LawyerAvailabilityResponse> availabilities = activeOnly 
                ? scheduleService.getActiveLawyerAvailabilities(lawyerId)
                : scheduleService.getLawyerAvailabilities(lawyerId);
        
        ApiResponse<List<LawyerAvailabilityResponse>> apiResponse = ApiResponse.<List<LawyerAvailabilityResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy khung giờ làm việc của luật sư thành công")
                .data(availabilities)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/schedule/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<Object>> getLawyerSchedule(
            @PathVariable Long lawyerId,
            @RequestParam String date) {
        
        Map<String, Object> timeSlot = new HashMap<>();
        timeSlot.put("startTime", "09:00:00");
        timeSlot.put("endTime", "10:00:00");
        timeSlot.put("status", "AVAILABLE");
        
        Map<String, Object> response = new HashMap<>();
        response.put("lawyerId", lawyerId);
        response.put("scheduleDate", date);
        response.put("availableSlots", List.of(timeSlot));
        response.put("bookedSlots", List.of());
        response.put("blockedSlots", List.of());
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy lịch làm việc thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/check-availability/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<Object>> checkLawyerAvailability(
            @PathVariable Long lawyerId,
            @RequestParam String dateTime,
            @RequestParam(defaultValue = "60") Integer durationMinutes) {
        
        boolean isAvailable = true; // Mock availability check
        String message = isAvailable ? "Luật sư có thể nhận lịch hẹn" : "Luật sư không có thời gian trong khung giờ này";
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(message)
                .data(isAvailable)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}