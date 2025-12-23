package com.example.chatservice.kafka;

import com.example.chatservice.client.UserClient;
import com.example.chatservice.dto.UserSummary;
import com.example.chatservice.event.ConversationAvatarUploadEvent;
import com.example.chatservice.event.FileUploadedEvent;
import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.Conversation.ConversationType;
import com.example.chatservice.jpa.repository.ConversationRepository;
import com.example.chatservice.mongo.document.Message;
import com.example.chatservice.mongo.document.Message.FileMeta;
import com.example.chatservice.mongo.repository.MessageRepository;
import com.example.chatservice.mongo.document.Message.MessageType;
import com.example.chatservice.mongo.document.Message.MessageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadedEventConsumer {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserClient userClient;

    @Value("${file.public-base-url}")
    private String publicFileBaseUrl;

    @KafkaListener(
            topics = "file-uploaded",
            containerFactory = "kafkaListenerFactory"
    )
    public void consume(FileUploadedEvent event, Acknowledgment ack) {

        try {
            switch (event.getBusinessType()) {

                case "GROUP_AVATAR" -> handleGroupAvatar(event);

                case "GROUP_SEND_FILE" -> handleGroupSendFile(event);

                default -> log.warn(
                        "⚠️ Unhandled businessType: {}",
                        event.getBusinessType()
                );
            }

            ack.acknowledge();

        } catch (Exception e) {
            log.error("❌ Error handling FileUploadedEvent", e);
            throw e; // retry
        }
    }

    // ================= GROUP AVATAR =================

    private void handleGroupAvatar(FileUploadedEvent event) {

        String conversationId = event.getBusinessId();

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElse(null);

        if (conversation == null) {
            log.warn("Conversation {} not found", conversationId);
            return;
        }

        if (conversation.getType() != ConversationType.GROUP || !conversation.isActive()) {
            log.warn("Conversation {} not eligible for avatar update", conversationId);
            return;
        }

        String avatarUrl = buildPublicFileUrl(event);

        conversation.setAvatarUrl(avatarUrl);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

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

        log.info("✅ Group avatar updated: {}", conversationId);
    }

    // ================= SEND FILE MESSAGE =================

    private void handleGroupSendFile(FileUploadedEvent event) {

        String conversationId = event.getBusinessId();

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new IllegalStateException("Conversation not found"));

        String fileUrl = buildPublicFileUrl(event);

        FileMeta fileMeta = new FileMeta(
                event.getFileId(),
                event.getFileName(),
                fileUrl,
                event.getFileSize(),
                event.getContentType()
        );

        Long senderId = event.getSenderId();
        UserSummary userSummary = userClient.getUserById(senderId);

        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(event.getSenderId())
                .senderName(userSummary.getFullName())
                .senderAvatar(userSummary.getAvatar())
                .type(MessageType.FILE)
                .file(fileMeta)
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        // update conversation last message
        conversation.setLastMessageAt(message.getTimestamp());
        conversation.setLastMessageText("📎 " + event.getFileName());
        conversation.setLastMessageSenderName(userSummary.getFullName());
        conversationRepository.save(conversation);

        // realtime
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversationId,
                message
        );

        log.info("✅ File message sent: conversation={}, file={}",
                conversationId, event.getFileName());
    }

    private String buildPublicFileUrl(FileUploadedEvent event) {
        return publicFileBaseUrl
                + "/" + event.getBucket()
                + "/" + event.getObjectKey();
    }
}
