package com.example.userservice.appointment.dto;

import com.example.userservice.appointment.entity.Appointment.AppointmentStatus;
import com.example.userservice.appointment.entity.Appointment.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    
    private Long appointmentId;
    private Long citizenId;
    private String citizenName;
    private String citizenPhone;
    private Long lawyerId;
    private String lawyerName;
    private String lawyerPhone;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private Integer durationMinutes;
    private String description;
    private AppointmentStatus status;
    private ConsultationType consultationType;
    private String meetingLocation;
    private String rejectionReason;
    private String cancellationReason;
    private Integer rating;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
