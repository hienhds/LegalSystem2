package com.example.backend.case_management.entity;

import com.example.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "case_progress_updates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseProgressUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long updateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case legalCase; // Lưu ý: dùng tên legalCase để tránh trùng keyword 'case'

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}