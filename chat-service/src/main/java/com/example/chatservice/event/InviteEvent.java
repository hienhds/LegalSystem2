package com.example.chatservice.event;

import com.example.chatservice.jpa.entity.Conversation;
import lombok.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteEvent {
    private Long inviteId;
    private String conversationName;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private String receiverName;
    private String receiverAvatar;
    private String type;
    private String note;
    private LocalDateTime timestamp;
}