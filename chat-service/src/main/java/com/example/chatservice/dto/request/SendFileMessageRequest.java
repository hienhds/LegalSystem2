package com.example.chatservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendFileMessageRequest {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
}
