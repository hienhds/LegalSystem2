package com.example.scheduleservice.appointment.controller;

import com.example.scheduleservice.appointment.dto.AppointmentRequest;
import com.example.scheduleservice.appointment.dto.AppointmentResponse;
import com.example.scheduleservice.appointment.entity.Appointment.AppointmentStatus;
import com.example.scheduleservice.appointment.service.AppointmentService;
import com.example.scheduleservice.common.dto.ApiResponse;
import com.example.scheduleservice.security.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            HttpServletRequest httpRequest) {
        
        Long citizenId = securityUtil.getCurrentUserId(httpRequest);
        if (citizenId == null) {
            ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized - Invalid token")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }
        
        log.info("Creating appointment for citizen: {}", citizenId);
        
        AppointmentResponse response = appointmentService.createAppointment(request, citizenId);
        
        if (response == null) {
            ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Không thể tạo lịch hẹn. Vui lòng kiểm tra thông tin và thời gian.")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Đặt lịch thành công! Chờ luật sư xác nhận.")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AppointmentStatus status) {
        
        Long citizenId = securityUtil.getCurrentUserId(httpRequest);
        if (citizenId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<Page<AppointmentResponse>>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .timestamp(Instant.now())
                    .build()
            );
        }
        
        log.info("Getting appointments for citizen: {}", citizenId);
        
        Page<AppointmentResponse> appointments = appointmentService.getCitizenAppointments(
                citizenId, page, size, status);
        
        ApiResponse<Page<AppointmentResponse>> apiResponse = ApiResponse.<Page<AppointmentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách lịch hẹn thành công")
                .data(appointments)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/lawyer/{lawyerId}")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getLawyerAppointments(
            @PathVariable Long lawyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AppointmentStatus status) {
        
        log.info("Getting appointments for lawyer: {}", lawyerId);
        
        Page<AppointmentResponse> appointments = appointmentService.getLawyerAppointments(
                lawyerId, page, size, status);
        
        ApiResponse<Page<AppointmentResponse>> apiResponse = ApiResponse.<Page<AppointmentResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách lịch hẹn thành công")
                .data(appointments)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @PathVariable Long appointmentId,
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "false") boolean isLawyer) {
        
        Long userId = securityUtil.getCurrentUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .timestamp(Instant.now())
                    .build()
            );
        }
        
        log.info("Getting appointment {} for user {}", appointmentId, userId);
                
        AppointmentResponse appointment = appointmentService.getAppointmentById(
                appointmentId, userId, isLawyer);
        
        if (appointment == null) {
            ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Không tìm thấy lịch hẹn với ID: " + appointmentId)
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin lịch hẹn thành công")
                .data(appointment)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/confirm")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(
            @PathVariable Long appointmentId,
            HttpServletRequest httpRequest,
            @RequestBody(required = false) Map<String, Object> request) {
        
        Long lawyerId = securityUtil.getCurrentUserId(httpRequest);
        if (lawyerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .timestamp(Instant.now())
                    .build()
            );
        }
        
        log.info("Lawyer {} confirming appointment {}", lawyerId, appointmentId);
        
        String message = request != null ? (String) request.get("message") : null;
        AppointmentResponse response = appointmentService.confirmAppointment(
                appointmentId, lawyerId, message);
        
        if (response == null) {
            ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Không thể xác nhận lịch hẹn. Vui lòng kiểm tra lại.")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Xác nhận lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/reject")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rejectAppointment(
            @PathVariable Long appointmentId,
            HttpServletRequest httpRequest,
            @RequestBody Map<String, Object> request) {
        
        Long lawyerId = securityUtil.getCurrentUserId(httpRequest);
        if (lawyerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .timestamp(Instant.now())
                    .build()
            );
        }
        
        log.info("Lawyer {} rejecting appointment {}", lawyerId, appointmentId);
        
        String reason = (String) request.get("reason");
        AppointmentResponse response = appointmentService.rejectAppointment(
                appointmentId, lawyerId, reason);
        
        if (response == null) {
            ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Không thể từ chối lịch hẹn. Vui lòng kiểm tra lại.")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Từ chối lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long appointmentId,
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "false") boolean isLawyer,
            @RequestBody Map<String, Object> request) {
        
        Long userId = securityUtil.getCurrentUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .timestamp(Instant.now())
                    .build()
            );
        }
        
        log.info("User {} cancelling appointment {}", userId, appointmentId);
        
        String reason = (String) request.get("reason");
                
        AppointmentResponse response = appointmentService.cancelAppointment(
                appointmentId, userId, isLawyer, reason);
        
        if (response == null) {
            ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Không thể hủy lịch hẹn. Vui lòng kiểm tra lại.")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Hủy lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(
            @PathVariable Long appointmentId,
            HttpServletRequest httpRequest) {
        
        Long lawyerId = securityUtil.getCurrentUserId(httpRequest);
        if (lawyerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .timestamp(Instant.now())
                    .build()
            );
        }
        
        log.info("Lawyer {} completing appointment {}", lawyerId, appointmentId);
        
        AppointmentResponse response = appointmentService.completeAppointment(
                appointmentId, lawyerId);
        
        if (response == null) {
            ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Không thể hoàn thành lịch hẹn. Vui lòng kiểm tra lại.")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Hoàn thành lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{appointmentId}/rate")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rateAppointment(
            @PathVariable Long appointmentId,
            HttpServletRequest httpRequest,
            @RequestBody Map<String, Object> request) {
        
        Long citizenId = securityUtil.getCurrentUserId(httpRequest);
        if (citizenId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .timestamp(Instant.now())
                    .build()
            );
        }
        
        log.info("Citizen {} rating appointment {}", citizenId, appointmentId);
        
        Integer rating = (Integer) request.get("rating");
        String comment = (String) request.get("comment");
        
        AppointmentResponse response = appointmentService.rateAppointment(
                appointmentId, citizenId, rating, comment);
        
        if (response == null) {
            ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Không thể đánh giá lịch hẹn. Vui lòng kiểm tra lại.")
                    .data(null)
                    .timestamp(Instant.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
        
        ApiResponse<AppointmentResponse> apiResponse = ApiResponse.<AppointmentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Đánh giá lịch hẹn thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
}