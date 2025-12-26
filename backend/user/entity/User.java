package com.example.backend.user.entity;

import com.example.backend.auth.entity.RefreshToken;
import com.example.backend.auth.entity.UserToken;
import com.example.backend.lawyer.entity.Lawyer;
import com.example.backend.search.entity.SearchHistory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
public class User {


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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserRole> userRoles = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserToken> userTokens = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Lawyer lawyer;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SearchHistory> searchHistories = new ArrayList<>();

    public User(){

    }

    public String getRoleName() {
        if (userRoles != null && !userRoles.isEmpty()) {
            // Ưu tiên trả về LAWYER nếu có
            for (UserRole userRole : userRoles) {
                if ("LAWYER".equals(userRole.getRole().getRoleName())) {
                    return "LAWYER";
                }
            }
            // Ưu tiên ADMIN nếu không có LAWYER
            for (UserRole userRole : userRoles) {
                if ("ADMIN".equals(userRole.getRole().getRoleName())) {
                    return "ADMIN";
                }
            }
            // Nếu không có LAWYER hay ADMIN, trả về role đầu tiên
            return userRoles.get(0).getRole().getRoleName();
        }
        return null;
    }
}
