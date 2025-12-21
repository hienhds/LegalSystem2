package com.example.userservice.common.dto;

import lombok.Data;

@Data
public class FileUploadRequest {
    private String fileName;
    private String contentType;
    private Long fileSize;
}
