package com.example.userservice.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerAvailabilityResponse {
    
    private Long availabilityId;
    private Long lawyerId;
    private String lawyerName;
    private Integer dayOfWeek;
    private String dayOfWeekName; // "Thứ 2", "Thứ 3", ...
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
    private String timeZone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
