package com.example.scheduleservice.appointment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "lawyer_availabilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availabilityId;
    
    @Column(name = "lawyer_id", nullable = false)
    private Long lawyerId;
    
    @Column(nullable = false)
    private Integer dayOfWeek; // 1 = Monday, 7 = Sunday
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Builder.Default
    private String timeZone = "Asia/Ho_Chi_Minh";
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
