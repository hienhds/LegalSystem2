package com.example.caseservice.dto;

import lombok.Data;

@Data
public class CreateCaseRequest {
    private String title;
    private String description;
    private Long clientId;
    // lawyerId sẽ được lấy từ Token của người đang đăng nhập (là luật sư)
}