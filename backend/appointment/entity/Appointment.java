package com.example.backend.appointment.entity;

import com.example.backend.user.entity.User;
import com.example.backend.lawyer.entity.Lawyer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    private Lawyer lawyer;
    
    @Column(nullable = false)
    private LocalDate appointmentDate;
    
    @Column(nullable = false)
    private LocalTime appointmentTime;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultationType consultationType;
    
    private String meetingLocation;
    
    @Builder.Default
    private Integer durationMinutes = 60;
    
    private String rejectionReason;
    
    private String cancellationReason;
    
    // Rating fields
    private Integer rating;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum AppointmentStatus {
        PENDING,
        CONFIRMED, 
        REJECTED,
        CANCELLED,
        COMPLETED
    }
    
    public enum ConsultationType {
        ONLINE,
        OFFLINE
    }
}