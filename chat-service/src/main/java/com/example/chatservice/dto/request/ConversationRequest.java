package com.example.chatservice.dto.request;

import com.example.chatservice.jpa.entity.Conversation;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty(value = "participantIds", access = JsonProperty.Access.WRITE_ONLY)
    private Set<Long> participantIds;
    
    @JsonProperty("memberIds")
    private void setMemberIds(Set<Long> memberIds) {
        this.participantIds = memberIds;
    }
    
    private Conversation.ConversationType type;
    private String note;
}
