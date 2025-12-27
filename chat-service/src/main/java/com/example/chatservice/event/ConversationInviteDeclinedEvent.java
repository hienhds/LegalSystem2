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
public class ConversationInviteDeclinedEvent {
    private String inviteId;
    private String conversationName;
    private Long userId;
    private Long ownerId;
    private String userFullName;
    private String userAvatar;
    private LocalDateTime declineAt;
}
