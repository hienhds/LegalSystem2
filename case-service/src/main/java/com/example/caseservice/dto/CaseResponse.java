package com.example.caseservice.dto;

import com.example.caseservice.entity.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List; // QUAN TRỌNG: Phải có dòng này

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseResponse {
    private Long id;
    private String title;
    private String description;
    private CaseStatus status;
    private Long lawyerId;
    private Long clientId;
    private String lawyerName;
    private String clientName;
    private LocalDateTime createdAt;
    private List<CaseUpdateResponse> progressUpdates; // Bây giờ sẽ hết lỗi
    private List<CaseDocumentResponse> documents;    // Bây giờ sẽ hết lỗi
}