package com.example.notificationservice.controller;

import com.example.notificationservice.dto.response.ApiResponse;
import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /* ================= LOAD BAN ĐẦU ================= */

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) Notification.NotificationType type,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request
    ) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Page<NotificationResponse> data =
                notificationService.getNotifications(userId, page, size, read, type, keyword);

        return ResponseEntity.ok(
                ApiResponse.<Page<NotificationResponse>>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Load notifications successfully")
                        .data(data)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    // xem chi tiet tuwng thong bao
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> viewDetailNotification(
            @PathVariable String id,
            HttpServletRequest request
    ){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        NotificationResponse notificationResponse = notificationService.detailNotification(userId, id);

        ApiResponse response = ApiResponse.<NotificationResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lay thanh coong")
                .data(notificationResponse)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // so luong thong bao chua doc
    // load lan dau vao trang
    @GetMapping("/unread/count")
    public long unreadCount() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }


}
