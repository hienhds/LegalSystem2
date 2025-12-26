package com.example.backend.lawyer.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateLawyerProfileRequest {

    private String barLicenseId;
    private String bio;
    private String officeAddress;

    @Min(value = 0, message = "yearsOfExp cannot be negative")
    private Integer yearsOfExp;

    private Long barAssociationId;

    // User fields
    private String fullName;
    private String phoneNumber;
    private String address;
}
