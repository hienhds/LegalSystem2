package com.example.userservice.appointment.dto;

import com.example.userservice.appointment.entity.Appointment.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableSlotResponse {
    
    private LocalDate date;
    private Long lawyerId;
    private String lawyerName;
    private Integer totalAvailableSlots;
    private List<TimeSlot> availableSlots;
    private List<BookedSlot> bookedSlots;
    private String message; // For informational messages or errors
    private Boolean hasWorkingSchedule; // Indicates if lawyer has schedule for this day
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer durationMinutes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookedSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private Long appointmentId;
        private AppointmentStatus status;
        private Integer durationMinutes;
    }
}
