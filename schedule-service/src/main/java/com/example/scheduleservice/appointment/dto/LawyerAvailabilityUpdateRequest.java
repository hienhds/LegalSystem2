package com.example.scheduleservice.appointment.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerAvailabilityUpdateRequest {
    
    @NotNull(message = "Day of week is required")
    @Min(value = 1, message = "Day of week must be between 1-7")
    @Max(value = 7, message = "Day of week must be between 1-7")
    private Integer dayOfWeek;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    private String workAddress;
    
    private String workAddressDetails;
    
    private Boolean isActive;
}
