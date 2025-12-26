package com.example.backend.case_management.dto;

import lombok.Data;

@Data
public class CreateCaseRequest {
    private String title;
    private String description;
    private Long lawyerId; // ID của luật sư mà khách hàng chọn
}