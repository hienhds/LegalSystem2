package com.example.chatservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationAvatarUploadEvent {
    private String conversationId;
    private String avatarUrl;
    private Long updateByUserId;
    private LocalDateTime upload;
}
