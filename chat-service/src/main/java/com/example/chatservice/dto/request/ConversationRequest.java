package com.example.chatservice.dto.request;

import com.example.chatservice.jpa.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRequest {
    private String conversationName;
    private Set<Long> participantIds;
    private Conversation.ConversationType type;
    private String note;
}
