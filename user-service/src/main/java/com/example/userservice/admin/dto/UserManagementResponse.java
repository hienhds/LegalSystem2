package com.example.userservice.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String avatarUrl;
    private Boolean isActive;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String role;
    private LocalDateTime createdAt;
}
