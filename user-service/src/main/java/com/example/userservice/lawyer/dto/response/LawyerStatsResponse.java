package com.example.userservice.lawyer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LawyerStatsResponse {
    private Long totalLawyers;
    private Long activeLawyers;
    private Long verifiedLawyers;
    private Double averageRating;
    private Long totalReviews;
    private Double satisfactionRate; // Percentage of completed appointments
    private Long totalAppointments;
    private Long completedAppointments;
}
