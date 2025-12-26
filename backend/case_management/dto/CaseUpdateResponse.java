package com.example.backend.case_management.dto;

import com.example.backend.case_management.entity.CaseProgressUpdate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CaseUpdateResponse {
    private Long updateId;
    private String title;
    private String description;
    private String createdByName;
    private LocalDateTime createdAt;

    public static CaseUpdateResponse from(CaseProgressUpdate update) {
        return CaseUpdateResponse.builder()
                .updateId(update.getUpdateId())
                .title(update.getTitle())
                .description(update.getDescription())
                .createdByName(update.getCreatedBy().getFullName())
                .createdAt(update.getCreatedAt())
                .build();
    }
}