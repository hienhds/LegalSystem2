package com.example.backend.lawyer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LawyerReviewResponse {
    private Long appointmentId;
    private String citizenName;
    private String citizenAvatar;
    private Integer rating;
    private String reviewComment;
    private LocalDateTime reviewedAt;
}
