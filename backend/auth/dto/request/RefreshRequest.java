package com.example.backend.auth.dto.request;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
