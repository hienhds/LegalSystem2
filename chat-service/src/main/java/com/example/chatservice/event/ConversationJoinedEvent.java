package com.example.chatservice.event;

import com.example.chatservice.jpa.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationJoinedEvent {
    private String conversationId;
    private Long userId;
    private String conversationName;
    private Conversation.ConversationType type;
    private LocalDateTime joinedAt;
}
