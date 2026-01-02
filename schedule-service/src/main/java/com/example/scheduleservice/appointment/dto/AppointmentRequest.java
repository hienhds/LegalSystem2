package com.example.scheduleservice.appointment.dto;

import com.example.scheduleservice.appointment.entity.Appointment.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequest {
    
    @NotNull(message = "Lawyer ID is required")
    private Long lawyerId;
    
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Appointment time is required")
    private LocalTime appointmentTime;
    
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Consultation type is required")
    private ConsultationType consultationType;
    
    @Size(max = 200, message = "Meeting location must not exceed 200 characters")
    private String meetingLocation;
    
    @Min(value = 30, message = "Duration must be at least 30 minutes")
    @Max(value = 240, message = "Duration must not exceed 240 minutes")
    @Builder.Default
    private Integer durationMinutes = 60;
}
