package com.example.caseservice.dto;

import com.example.caseservice.entity.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseEvent {
    private String eventType; // CREATE, UPDATE, DELETE
    private Long id;
    private String title;
    private String description;
    private CaseStatus status;
    
    // Thông tin Luật sư (lấy từ user-service)
    private Long lawyerId;
    private String lawyerName;
    private String lawyerEmail;
    private String lawyerPhone;

    // Thông tin Khách hàng (lấy từ user-service)
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;

    private LocalDateTime createdAt;
}