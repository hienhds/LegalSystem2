package com.example.notificationservice.service;

import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.event.NotificationEvent;
import com.example.notificationservice.event.UnreadCountEvent;
import com.example.notificationservice.kafka.NotificationEventProducer;
import com.example.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationEventProducer notificationEventProducer;

    public void notifyUser(Long userId, String title, String content, Notification.NotificationType type){
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .read(false)
                .createdAt(LocalDateTime.now())
                .type(type)
                .build();

        notification = notificationRepository.save(notification);

        NotificationEvent event = NotificationEvent.builder()
                .id(notification.getId())
                .userId(userId)
                .content(content)
                .title(title)
                .read(false)
                .createdAt(notification.getCreatedAt())
                .type(type)
                .build();
        notificationEventProducer.publishNotificationCreated(event);

        Long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);

        UnreadCountEvent unreadCountEvent = UnreadCountEvent.builder()
                .userId(userId)
                .unreadCount(unreadCount)
                .build();
        notificationEventProducer.publishUnreadCountNotification(unreadCountEvent);
    }


    // Hiển thị chi tiet thoong bao va danh dau laf da doc
    public NotificationResponse detailNotification(Long userId, String notificationId){
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Forbidden");
        }

        if(notification.isRead() == false){
            notification.setRead(true);

            notificationRepository.save(notification);

            Long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);

            UnreadCountEvent unreadCountEvent = UnreadCountEvent.builder()
                    .userId(userId)
                    .unreadCount(unreadCount)
                    .build();
            notificationEventProducer.publishUnreadCountNotification(unreadCountEvent);
        }



        NotificationResponse response = NotificationResponse.builder()
                .id(notificationId)
                .content(notification.getContent())
                .title(notification.getTitle())
                .userId(userId)
                .type(notification.getType())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();

        return response;
    }

    /* ================= LOAD BAN ĐẦU ================= */

    public Page<NotificationResponse> getNotifications(
            Long userId,
            int page,
            int size,
            Boolean read,
            Notification.NotificationType type,
            String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return notificationRepository
                .searchNotifications(userId, read, type, keyword, pageable)
                .map(this::toResponse);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .title(n.getTitle())
                .content(n.getContent())
                .type(n.getType())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }


}
