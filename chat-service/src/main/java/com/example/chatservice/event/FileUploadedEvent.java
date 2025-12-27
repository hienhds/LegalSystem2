package com.example.chatservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadedEvent {

    // ==== file ====
    private String fileId;
    private String bucket;
    private String objectKey;
    private String fileName;
    private Long fileSize;
    private String contentType;

    // ==== business ====
    private String businessType;   // GROUP_AVATAR | GROUP_SEND_FILE
    private String businessId;     // conversationId

    // ==== sender ====
    private Long senderId;
}
