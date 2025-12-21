package com.example.chatservice.dto.response;

import com.example.chatservice.jpa.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {

    private String id; // UUID string

    private String name;  // neeus la private tu clent tu render theo ten cua doi phuong

    private Conversation.ConversationType type; // PRIVATE, GROUP

    private Long creatorId;

    private String creatorFullName;

    private String creatorAvatar;

    private String avatarUrl; // // neeus la private tu clent tu render theo ten cua doi phuong

    private boolean isLocked = false;

    private String lastMessageText;

    private String lastMessageSenderName;

    private LocalDateTime lastMessageAt;

    private Set<Long> members = new HashSet<>();

}
