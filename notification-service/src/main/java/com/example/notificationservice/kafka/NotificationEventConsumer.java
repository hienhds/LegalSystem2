package com.example.notificationservice.kafka;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.event.*;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @KafkaListener(
            topics = "notification-created",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void handleNotificationCreated(NotificationEvent event, Acknowledgment ack) {
        messagingTemplate.convertAndSend(
                "/user/" + event.getUserId() + "/queue/notification",
                event
        );
        ack.acknowledge();
    }

    @KafkaListener(
            topics = "notification-unread-count",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void handleUnreadCount(UnreadCountEvent event) {
        messagingTemplate.convertAndSend(
                "/user/" + event.getUserId() + "/queue/notification-count",
                event.getUnreadCount()
        );
    }

    @KafkaListener(
            topics = "conversation-created",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void handleConversationCreated(ConversationCreatedEvent event, Acknowledgment ack) {
        notificationService.notifyUser(
                event.getCreatorId(),
                "Tạo phòng thành công",
                "Bạn đã tạo phòng: " + event.getConversationName()
                        + ". Vào lúc " + event.getCreatedAt(),
                Notification.NotificationType.CONVERSATION_CREATED
        );
        // Xác nhận đã xử lý xong
        ack.acknowledge();
    }

    @KafkaListener(
            topics = "member-removed",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void handleNotificationRemoveMember(RemoveMemberEvent event) {
        notificationService.notifyUser(
                event.getMemberId(),
                "Bị Xóa Khỏi Phòng",
                "Bạn đã bị mời ra khỏi phòng: " + event.getConversationName()
                        + ". Vào lúc " + event.getTimestamp(),
                Notification.NotificationType.REMOVED
        );
    }

    @KafkaListener(
            topics = "invite-created",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void handleInviteCreated(InviteEvent event) {
        notificationService.notifyUser(
                event.getReceiverId(),
                "Lời mời tham gia phòng",
                "Bạn được mời tham gia phòng: " + event.getConversationName(),
                Notification.NotificationType.INVITE
        );
    }

    @KafkaListener(
            topics = "group-dissolved",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void handleConversationDissolved(DissolveGroupEvent event) {
        for (Long memberId : event.getMemberIds()) {
            notificationService.notifyUser(
                    memberId,
                    "Giải Tán Phòng",
                    "Phòng: " + event.getConversationName()
                            + " đã bị giải tán. Vào lúc " + event.getTimestamp(),
                    Notification.NotificationType.DISSOLVED
            );
        }
    }

    @KafkaListener(
            topics = "conversation-decline",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void handleRejectInvite(ConversationInviteDeclinedEvent event) {
        notificationService.notifyUser(
                event.getOwnerId(),
                "Từ chối lời mời",
                "Bạn " + event.getUserFullName()
                        + " đã từ chối tham gia phòng: "
                        + event.getConversationName(),
                Notification.NotificationType.REJECT
        );
    }
}
