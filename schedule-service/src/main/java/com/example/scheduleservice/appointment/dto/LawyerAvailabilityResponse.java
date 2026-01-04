package com.example.scheduleservice.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerAvailabilityResponse {
    
    private Long availabilityId;
    private Long lawyerId;
    private Integer dayOfWeek;
    private String dayOfWeekName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String workAddress;
    private String workAddressDetails;
    private Boolean isActive;
    private String timeZone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public String getDayOfWeekName() {
        return switch (dayOfWeek) {
            case 1 -> "Thứ 2";
            case 2 -> "Thứ 3";
            case 3 -> "Thứ 4";
            case 4 -> "Thứ 5";
            case 5 -> "Thứ 6";
            case 6 -> "Thứ 7";
            case 7 -> "Chủ nhật";
            default -> "Unknown";
        };
    }
}
