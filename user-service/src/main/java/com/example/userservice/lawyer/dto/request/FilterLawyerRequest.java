package com.example.userservice.lawyer.dto.request;


import com.example.userservice.lawyer.entity.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FilterLawyerRequest {
    // loc theo trang thai
    private VerificationStatus status;
    private Long barAssociationId;
    private String keyword;
    
    // Filter by specializations
    private List<Long> specializationIds;
    
    // Filter by experience
    private Integer minYearsOfExp;
    private Integer maxYearsOfExp;
    
    // Filter by rating
    private Double minRating;
    
    // Sorting
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
