package com.example.chatservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationCreatedEvent {
    private String conversationId;
    private String conversationName;
    private Set<Long> memberIds;
    private Long creatorId;
    private String creatorName;
    private String creatorAvatar;
    private LocalDateTime createdAt;
}
