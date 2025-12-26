package com.example.userservice.appointment.controller;

import com.example.userservice.appointment.dto.AppointmentRequest;
import com.example.userservice.appointment.dto.AppointmentResponse;
import com.example.userservice.appointment.entity.Appointment.AppointmentStatus;
import com.example.userservice.appointment.service.AppointmentService;
import com.example.userservice.common.dto.ApiResponse;
import com.example.userservice.common.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        
        Long citizenId = user.getUser().getUserId();
        log.info("Creating appointment for citizen: {}", citizenId);
        
        AppointmentResponse response = appointmentService.createAppointment(request, citizenId);
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Đặt lịch thành công! Chờ luật sư xác nhận.")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AppointmentStatus status,
            HttpServletRequest httpRequest) {
        
        Long userId = user.getUser().getUserId();
        log.info("Getting appointments for user: {}", userId);
        
        Page<AppointmentResponse> appointments = appointmentService.getCitizenAppointments(
                userId, page, size, status);
        
        ApiResponse<Page<AppointmentResponse>> apiResponse = ApiResponse.<Page<AppointmentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách lịch hẹn thành công")
                .data(appointments)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/lawyer-appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getLawyerAppointments(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AppointmentStatus status,
            HttpServletRequest httpRequest) {
        
        // Assume lawyer can be retrieved from user - adjust based on your data model
        Long userId = user.getUser().getUserId();
        log.info("Getting appointments for lawyer user: {}", userId);
        
        // TODO: Get lawyerId from user
        Long lawyerId = userId; // Placeholder
        
        Page<AppointmentResponse> appointments = appointmentService.getLawyerAppointments(
                lawyerId, page, size, status);
        
        ApiResponse<Page<AppointmentResponse>> apiResponse = ApiResponse.<Page<AppointmentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách lịch hẹn thành công")
                .data(appointments)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "false") boolean isLawyer,
            HttpServletRequest httpRequest) {
        
        Long userId = user.getUser().getUserId();
        log.info("Getting appointment {} for user {}", appointmentId, userId);
                
        AppointmentResponse appointment = appointmentService.getAppointmentById(
                appointmentId, userId, isLawyer);
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin lịch hẹn thành công")
                .data(appointment)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/confirm")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody(required = false) Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        // TODO: Get lawyerId from user
        Long lawyerId = user.getUser().getUserId();
        log.info("Lawyer {} confirming appointment {}", lawyerId, appointmentId);
        
        String message = request != null ? (String) request.get("message") : null;
        AppointmentResponse response = appointmentService.confirmAppointment(
                appointmentId, lawyerId, message);
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Xác nhận lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/reject")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rejectAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        // TODO: Get lawyerId from user
        Long lawyerId = user.getUser().getUserId();
        log.info("Lawyer {} rejecting appointment {}", lawyerId, appointmentId);
        
        String reason = (String) request.get("reason");
        AppointmentResponse response = appointmentService.rejectAppointment(
                appointmentId, lawyerId, reason);
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Từ chối lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "false") boolean isLawyer,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        Long userId = user.getUser().getUserId();
        log.info("User {} cancelling appointment {}", userId, appointmentId);
        
        String reason = (String) request.get("reason");
                
        AppointmentResponse response = appointmentService.cancelAppointment(
                appointmentId, userId, isLawyer, reason);
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Hủy lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal CustomUserDetails user,
            HttpServletRequest httpRequest) {
        
        // TODO: Get lawyerId from user
        Long lawyerId = user.getUser().getUserId();
        log.info("Lawyer {} completing appointment {}", lawyerId, appointmentId);
        
        AppointmentResponse response = appointmentService.completeAppointment(
                appointmentId, lawyerId);
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Hoàn thành lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/rate")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rateAppointment(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        Long citizenId = user.getUser().getUserId();
        log.info("Citizen {} rating appointment {}", citizenId, appointmentId);
        
        Integer rating = (Integer) request.get("rating");
        String comment = (String) request.get("comment");
        
        AppointmentResponse response = appointmentService.rateAppointment(
                appointmentId, citizenId, rating, comment);
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Đánh giá lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}
