package com.example.notificationservice.event;

import com.example.notificationservice.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private String id; // UUID string
    private Long userId;
    private String title;
    private String content;
    @Builder.Default
    private boolean read = false;
    private LocalDateTime createdAt;
    private Notification.NotificationType type;

}
