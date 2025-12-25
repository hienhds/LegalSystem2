package com.example.caseservice.entity;

import jakarta.persistence.*;
import lombok.*;
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
    private Long id;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private Case legalCase;

    @Column(columnDefinition = "TEXT")
    private String updateDescription;

    private LocalDateTime updateDate;

    @PrePersist
    protected void onCreate() {
        updateDate = LocalDateTime.now();
    }
}