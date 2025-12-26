package com.example.backend.lawyer.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateLawyerRequest {
    private String bio;
    private String officeAddress;
    private String certificateImageUrl;
    private Integer yearsOfExp;
    private UUID barAssociationId;
    private List<UUID> specializationIds;
}
