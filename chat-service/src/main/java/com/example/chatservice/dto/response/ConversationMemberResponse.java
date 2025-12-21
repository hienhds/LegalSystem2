package com.example.chatservice.dto.response;

import com.example.chatservice.jpa.entity.ConversationMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationMemberResponse {

    private String id; // UUID string

    private String conversationId;

    private Long userId;
    private String userName;
    private String userAvatar;
    private ConversationMember.MemberStatus memberStatus;


}
