package com.example.backend.lawyer.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LawyerResponse {

    private Long lawyerId;
    private String fullName;
    private String barLicenseId;
    private String bio;
    private String certificateImageUrl;
    private String officeAddress;
    private int yearsOfExp;
    private String barAssociationName;
    private String verificationStatus;
    private LocalDateTime createdAt;
}
