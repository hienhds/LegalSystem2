package com.example.notificationservice.dto.response;

import com.example.notificationservice.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {

    private String id; // UUID string

    private Long userId;

    private String title;

    private String content;

    private boolean read = false;

    private LocalDateTime createdAt;

    private Notification.NotificationType type;

}
