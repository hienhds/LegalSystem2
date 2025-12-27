package com.example.userservice.user.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String avatarUrl;
    private String address;
}
