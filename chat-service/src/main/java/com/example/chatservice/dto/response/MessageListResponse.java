package com.example.chatservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageListResponse {
    private List<MessageResponse> items;
    private boolean hasMore;
    private String nextCursor;
}
