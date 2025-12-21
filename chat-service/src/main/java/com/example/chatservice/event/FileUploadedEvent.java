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
    private String fileId;
    private String bucket;
    private String objectKey;
    private String businessType;
    private String businessId;
    private String contentType;
}
