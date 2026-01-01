package com.example.caseservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    @JsonAlias("userId") // Map từ "userId" của user-service sang "id"
    private Long id;
    
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Set<String> roles;
}