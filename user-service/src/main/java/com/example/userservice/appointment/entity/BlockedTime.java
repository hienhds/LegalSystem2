package com.example.userservice.appointment.entity;

import com.example.userservice.lawyer.entity.Lawyer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_times")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedTime {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockedTimeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    private Lawyer lawyer;
    
    @Column(nullable = false)
    private LocalDate blockedDate;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false) 
    private LocalTime endTime;
    
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BlockType blockType = BlockType.MANUAL;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum BlockType {
        MANUAL,        // Luật sư tự block
        APPOINTMENT,   // Block do có lịch hẹn
        HOLIDAY,       // Ngày lễ
        VACATION       // Nghỉ phép
    }
}
