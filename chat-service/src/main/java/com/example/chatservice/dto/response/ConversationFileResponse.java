package com.example.chatservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationFileResponse {
    private String messageId;
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private Long senderId;
    private String senderName;
    private LocalDateTime sentAt;
}
