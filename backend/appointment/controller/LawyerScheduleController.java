package com.example.backend.appointment.controller;

import com.example.backend.common.dto.ApiResponse;
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

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<Object>> createAvailability(@RequestBody Map<String, Object> request) {
        log.info("Creating availability: {}", request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("availabilityId", 1L);
        response.put("dayOfWeek", request.get("dayOfWeek"));
        response.put("startTime", request.get("startTime"));
        response.put("endTime", request.get("endTime"));
        response.put("isActive", true);
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Tạo khung giờ làm việc thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<Object>> updateAvailability(
            @PathVariable Long availabilityId,
            @RequestBody Map<String, Object> request) {
        
        log.info("Updating availability {}: {}", availabilityId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("availabilityId", availabilityId);
        response.put("dayOfWeek", request.get("dayOfWeek"));
        response.put("startTime", request.get("startTime"));
        response.put("endTime", request.get("endTime"));
        response.put("isActive", request.get("isActive"));
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Cập nhật khung giờ làm việc thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<Object>> deleteAvailability(@PathVariable Long availabilityId) {
        log.info("Deleting availability: {}", availabilityId);
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Xóa khung giờ làm việc thành công")
                .data(null)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<Object>> getLawyerAvailabilities() {
        
        Map<String, Object> mockAvailability = new HashMap<>();
        mockAvailability.put("availabilityId", 1L);
        mockAvailability.put("dayOfWeek", 1);
        mockAvailability.put("dayOfWeekName", "Thứ 2");
        mockAvailability.put("startTime", "09:00:00");
        mockAvailability.put("endTime", "17:00:00");
        mockAvailability.put("isActive", true);
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách khung giờ làm việc thành công")
                .data(List.of(mockAvailability))
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/availability/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<Object>> getSpecificLawyerAvailabilities(@PathVariable Long lawyerId) {
        
        Map<String, Object> mockAvailability = new HashMap<>();
        mockAvailability.put("availabilityId", 1L);
        mockAvailability.put("lawyerId", lawyerId);
        mockAvailability.put("dayOfWeek", 1);
        mockAvailability.put("startTime", "09:00:00");
        mockAvailability.put("endTime", "17:00:00");
        mockAvailability.put("isActive", true);
        
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy khung giờ làm việc của luật sư thành công")
                .data(List.of(mockAvailability))
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