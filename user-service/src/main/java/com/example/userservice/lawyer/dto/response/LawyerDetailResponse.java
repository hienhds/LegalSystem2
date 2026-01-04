package com.example.userservice.lawyer.dto.response;

import com.example.userservice.lawyer.entity.VerificationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class LawyerDetailResponse {
    private Long lawyerId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String address;

    private String barLicenseId;
    private String bio;
    private String certificateImageUrl;
    private String officeAddress;
    private int yearsOfExp;

    private Long barAssociationId;
    private String barAssociationName;
    private VerificationStatus verificationStatus;

    private List<String> specializationNames;
    private List<String> roles; // Add roles field for consistency with UserResponse

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
