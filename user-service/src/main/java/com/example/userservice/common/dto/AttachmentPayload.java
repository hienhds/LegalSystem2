package com.example.userservice.common.dto;

import lombok.Data;

@Data
public class AttachmentPayload {
    private String key;       // s3 key returned earlier
    private String fileName;
    private String fileType;
    private Long fileSize;
}
