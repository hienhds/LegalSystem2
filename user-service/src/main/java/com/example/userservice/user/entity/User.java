package com.example.userservice.user.entity;


import com.example.userservice.auth.entity.RefreshToken;
import com.example.userservice.auth.entity.UserToken;
import com.example.userservice.lawyer.entity.Lawyer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_phone", columnList = "phone_number")
        }
)
@Getter
@Setter
@AllArgsConstructor
@Builder
public class User implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    @NotBlank
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    @NotBlank
    private String fullName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;


    @Column(name = "avatar_url")
    private String avatarUrl;


    @Column(name = "address")
    private String address;


    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "phone_verified")
    private boolean phoneVerified = false;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_lawyer")
    private boolean isLawyer = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserToken> userTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Lawyer lawyer;

    public User() {
        // Default constructor
    }
    
    // Helper method for admin management
    public String getRoleName() {
        if (userRoles == null || userRoles.isEmpty()) {
            return "USER";
        }
        return userRoles.get(0).getRole().getRoleName();
    }

}
