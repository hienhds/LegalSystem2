package com.example.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalUsers;
    private Double userGrowth;
    private Long totalLawyers;
    private Double lawyerGrowth;
    private Long totalQuestions;
    private Double questionGrowth;
    private Long totalLegalDocs;
    private Double legalDocGrowth;
}
