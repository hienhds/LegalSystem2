package com.example.backend.user.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private String address;
}
