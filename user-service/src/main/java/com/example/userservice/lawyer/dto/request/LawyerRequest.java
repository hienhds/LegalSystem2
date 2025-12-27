package com.example.userservice.lawyer.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

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
