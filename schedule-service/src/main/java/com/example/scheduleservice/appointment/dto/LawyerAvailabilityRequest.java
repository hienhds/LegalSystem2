package com.example.scheduleservice.appointment.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerAvailabilityRequest {
    
    @NotNull(message = "Lawyer ID is required")
    private Long lawyerId;
    
    @NotEmpty(message = "At least one day of week must be selected")
    @Size(min = 1, max = 7, message = "Days of week must be between 1 and 7 days")
    private List<@Min(value = 1, message = "Day of week must be between 1-7") 
                 @Max(value = 7, message = "Day of week must be between 1-7") 
                 Integer> daysOfWeek;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @NotBlank(message = "Work address is required")
    @Size(max = 500, message = "Work address must not exceed 500 characters")
    private String workAddress;
    
    @Size(max = 200, message = "Work address details must not exceed 200 characters")
    private String workAddressDetails; // Số phòng, tầng, toà nhà...
    
    private String timeZone;
    
    @Builder.Default
    private Boolean isActive = true;
}
