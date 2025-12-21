package com.example.userservice.lawyer.dto.request;


import com.example.userservice.lawyer.entity.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterLawyerRequest {
    // loc theo trang thai
    private VerificationStatus status;
    private Long barAssociationId;
    private String keyword;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
