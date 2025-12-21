package com.example.userservice.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresignedUploadResponse {
    private String key;            // object key in S3
    private String presignedUrl;   // URL to PUT the file
    private Long expiresInSeconds; // TTL
    private String fileName;
    private String contentType;
}
