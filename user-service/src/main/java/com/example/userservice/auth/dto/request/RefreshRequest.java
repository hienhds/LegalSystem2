package com.example.userservice.auth.dto.request;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
