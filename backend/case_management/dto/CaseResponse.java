package com.example.backend.case_management.dto;

import com.example.backend.case_management.entity.Case;
import com.example.backend.case_management.entity.CaseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class CaseResponse {
    private Long caseId;
    private String title;
    private String description;
    private String clientName;
    private String lawyerName;
    private CaseStatus status;
    private LocalDateTime createdAt;

    // Thêm 2 trường này
    private List<CaseUpdateResponse> updates;
    private List<CaseDocumentResponse> documents;

    public static CaseResponse from(Case c) {
        return CaseResponse.builder()
                .caseId(c.getCaseId())
                .title(c.getTitle())
                .description(c.getDescription())
                .clientName(c.getClient().getFullName())
                .lawyerName(c.getLawyer().getFullName())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                // Map danh sách updates
                .updates(c.getUpdates() != null ?
                        c.getUpdates().stream()
                                .map(CaseUpdateResponse::from)
                                .collect(Collectors.toList()) : null)
                // Map danh sách documents
                .documents(c.getDocuments() != null ?
                        c.getDocuments().stream()
                                .map(CaseDocumentResponse::from)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}