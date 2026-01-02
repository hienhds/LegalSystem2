package com.example.userservice.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LawyerVerificationStatusResponse {
    private Long total;
    private Long verified;
    private Long pending;
    private Long rejected;
    private Double verifiedPercent;
    private Double pendingPercent;
    private Double rejectedPercent;
}
