package com.example.backend.lawyer.entity;

import com.example.backend.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lawyer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lawyer {

    @Id
    @Column(name = "lawyer_id")
    private Long lawyerId;  // camelCase đúng chuẩn

    // Shared Primary Key (1–1)
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "lawyer_id")
    private User user;

    @Column(nullable = false, unique = true, name = "bar_license_id")
    private String barLicenseId;

    @Column(name = "bio")
    private String bio;

    @Column(name = "certificate_image_url")
    private String certificateImageUrl;

    @Column(name = "office_address")
    private String officeAddress;

    @Column(name = "years_of_exp", nullable = false)
    private int yearsOfExp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_association_id", nullable = false)
    private BarAssociation barAssociation;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Many-to-Many dùng bảng trung gian LawyerSpecialization → không JoinTable
    @OneToMany(mappedBy = "lawyer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<LawyerSpecialization> specializations = new HashSet<>();
}
