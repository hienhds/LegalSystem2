package com.example.userservice.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerAvailabilityRequest {
    
    // lawyerId will be taken from authentication token, not from request body
    
    @NotNull(message = "Day of week is required")
    @Min(value = 1, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
    @Max(value = 7, message = "Day of week must be between 1 (Monday) and 7 (Sunday)")
    private Integer dayOfWeek; // 1 = Monday, 7 = Sunday
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private String timeZone;
}
