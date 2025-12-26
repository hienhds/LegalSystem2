package com.example.backend.lawyer.dto.response;


import com.example.backend.lawyer.entity.VerificationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
public class LawyerListResponse {

    private Long lawyerId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String barAssociationName;
    private String barLicenseId;
    private String certificateUrl;
    private VerificationStatus verificationStatus;
    private Integer yearsOfExp;
    private String bio;
    private List<String> specializations;
    private LocalDateTime createdAt;
    
    // Rating information from appointments
    private Double averageRating;
    private Long reviewCount;
    private String officeAddress;
}
