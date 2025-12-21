package com.example.chatservice.dto.response;

import com.example.chatservice.jpa.entity.ConversationInvite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationInviteResponse {
    private String id;

    private String conversationId;

    private Long receiverId;

    private String receiverName;

    private String receiverAvatar;

    private Long senderId; // who invited or who handled the request

    private String senderName;

    private String senderAvatar;

    private ConversationInvite.InviteStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime respondedAt;
    private String note;
}
