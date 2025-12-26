package com.example.userservice.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkAvailabilityRequest {
    
    @NotNull(message = "Days of week are required")
    @NotEmpty(message = "Days of week list cannot be empty")
    private List<@Min(value = 1, message = "Day of week must be between 1 and 7") 
                 @Max(value = 7, message = "Day of week must be between 1 and 7") Integer> dayOfWeeks;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private String timeZone;
}
