package com.example.userservice.common.dto;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private int status;
    private String message;
    private String errorCode;
    private T data;
    private Map<String, String> errors;
    private String path;
    private Instant timestamp;
    private String traceId;
    private Map<String, String> links;
}
