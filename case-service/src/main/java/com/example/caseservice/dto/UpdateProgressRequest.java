package com.example.caseservice.dto;

import com.example.caseservice.entity.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressRequest {
    private String description;
    private CaseStatus status; // Thêm trường này để service có thể gọi getStatus()
}