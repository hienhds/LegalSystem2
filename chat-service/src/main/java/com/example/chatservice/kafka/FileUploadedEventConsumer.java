package com.example.chatservice.kafka;

import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.Conversation.ConversationType;
import com.example.chatservice.event.ConversationAvatarUploadEvent;
import com.example.chatservice.event.FileUploadedEvent;
import com.example.chatservice.jpa.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadedEventConsumer {

    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${file.public-base-url}")
    private String publicFileBaseUrl;

    /**
     * Nhận event upload file từ MinIO / file-service
     * → Update avatar conversation
     * → Broadcast realtime qua WebSocket
     */
    @KafkaListener(
            topics = "file-uploaded",
            containerFactory = "kafkaListenerFactory"
    )
    public void consume(FileUploadedEvent event) {

        // 1️⃣ Chỉ xử lý avatar group
        if (!"GROUP_AVATAR".equals(event.getBusinessType())) {
            return;
        }

        String conversationId = event.getBusinessId();

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElse(null);

        if (conversation == null) {
            log.warn("Conversation {} not found for avatar update", conversationId);
            return;
        }

        // 2️⃣ Chỉ GROUP + active mới được đổi avatar
        if (conversation.getType() != ConversationType.GROUP
                || !conversation.isActive()) {
            log.warn("Conversation {} not eligible for avatar update", conversationId);
            return;
        }

        // 3️⃣ Build public URL (qua API Gateway)
        String avatarUrl = buildAvatarUrl(event);

        // 4️⃣ Update DB
        conversation.setAvatarUrl(avatarUrl);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        log.info("✅ Updated avatar for group {}", conversationId);

        // 5️⃣ Broadcast realtime cho tất cả member
        ConversationAvatarUploadEvent wsEvent =
                ConversationAvatarUploadEvent.builder()
                        .conversationId(conversationId)
                        .avatarUrl(avatarUrl)
                        .upload(LocalDateTime.now())
                        .build();

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId,
                wsEvent
        );
    }

    private String buildAvatarUrl(FileUploadedEvent event) {
        return publicFileBaseUrl
                + "/" + event.getBucket()
                + "/" + event.getObjectKey();
    }
}
