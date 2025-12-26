package com.example.backend.lawyer.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LawyerRequest {

    @NotBlank(message = "barLicenseId is required")
    private String barLicenseId;

    private String bio;
    private String officeAddress;

    @Min(value = 0, message = "yearsOfExp cannot be negative")
    private Integer yearsOfExp;

    @NotNull(message = "barAssociationId is required")
    private Long barAssociationId;

    @NotNull
    private List<Long> specializationIds;
}
