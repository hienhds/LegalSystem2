package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationCustomRepository {
    Page<Notification> searchNotifications(
            Long userId,
            Boolean read,
            Notification.NotificationType type,
            String keyword,
            Pageable pageable
    );
}
