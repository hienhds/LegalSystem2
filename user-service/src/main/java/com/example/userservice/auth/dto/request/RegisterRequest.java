package com.example.userservice.auth.dto.request;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String phoneNumber;
    private String fullName;
    private String password;
}
