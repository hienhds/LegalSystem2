package com.example.chatservice.dto.request;

import lombok.Data;

@Data
public class AddMemberRequest {
    private Long userId;
    private String userFullName;
    private String avatar;
}
