package com.example.backend.admin.controller;

import com.example.backend.admin.dto.DashboardStatsResponse;
import com.example.backend.admin.dto.LawyerVerificationStatusResponse;
import com.example.backend.admin.dto.RegistrationChartResponse;
import com.example.backend.admin.service.DashboardService;
import com.example.backend.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User accessing dashboard stats: {}", auth.getName());
        log.info("Authorities: {}", auth.getAuthorities());
        
        DashboardStatsResponse stats = dashboardService.getDashboardStats();

        ApiResponse<DashboardStatsResponse> response = ApiResponse.<DashboardStatsResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thống kê dashboard thành công")
                .data(stats)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/registrations")
    public ResponseEntity<ApiResponse<RegistrationChartResponse>> getRegistrationChart(
            @RequestParam(defaultValue = "7") int days,
            HttpServletRequest request) {
        
        RegistrationChartResponse chart = dashboardService.getRegistrationChart(days);

        ApiResponse<RegistrationChartResponse> response = ApiResponse.<RegistrationChartResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy biểu đồ đăng ký thành công")
                .data(chart)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/lawyer-verification-status")
    public ResponseEntity<ApiResponse<LawyerVerificationStatusResponse>> getLawyerVerificationStatus(
            HttpServletRequest request) {
        
        LawyerVerificationStatusResponse status = dashboardService.getLawyerVerificationStatus();

        ApiResponse<LawyerVerificationStatusResponse> response = ApiResponse.<LawyerVerificationStatusResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy tình trạng xác minh luật sư thành công")
                .data(status)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }
}
