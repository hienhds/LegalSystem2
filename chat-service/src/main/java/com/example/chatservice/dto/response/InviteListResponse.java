package com.example.chatservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InviteListResponse {
    private List<ConversationInviteResponse> items;
    private boolean hasMore;
    private String nextCursor;

}
