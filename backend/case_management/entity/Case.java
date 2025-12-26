package com.example.backend.case_management.entity;

import com.example.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long caseId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Khách hàng (Người dân)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    // Luật sư phụ trách
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    private User lawyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Quan hệ 1-N với Updates
    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaseProgressUpdate> updates = new ArrayList<>();

    // Quan hệ 1-N với Documents
    @OneToMany(mappedBy = "legalCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CaseDocument> documents = new ArrayList<>();
}