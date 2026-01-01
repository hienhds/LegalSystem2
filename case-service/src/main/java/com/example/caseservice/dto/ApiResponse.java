package com.example.caseservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    @JsonAlias("status") // Map từ "status" của user-service sang "code"
    private int code;
    
    private String message;
    
    @JsonAlias("data") // Map từ "data" của user-service sang "result"
    private T result;
}