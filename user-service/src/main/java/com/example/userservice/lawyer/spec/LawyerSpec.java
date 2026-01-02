package com.example.userservice.lawyer.spec;

import com.example.userservice.lawyer.entity.Lawyer;
import com.example.userservice.lawyer.entity.VerificationStatus;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import com.example.userservice.lawyer.entity.LawyerSpecialization;

import java.util.List;

public class LawyerSpec {
    public static Specification<Lawyer> filter(
            VerificationStatus verificationStatus,
            Long barAssociationId,
            String keyword,
            List<Long> specializationIds,
            Integer minYearsOfExp,
            Integer maxYearsOfExp,
            Double minRating
    ){
        return ((root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            
            // Filter by verification status
            if(verificationStatus != null){
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(root.get("verificationStatus"), verificationStatus));
            }

            // Filter by bar association
            if(barAssociationId != null){
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.equal(root.get("barAssociation").get("barAssociationId"), barAssociationId));
            }

            // Filter by keyword (search in license ID and full name)
            if(keyword != null && !keyword.isEmpty() && !keyword.isBlank()){
                Predicate p1 = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("barLicenseId")), 
                    "%" + keyword.toLowerCase() + "%");
                Predicate p2 = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("fullName")), 
                    "%" + keyword.toLowerCase() + "%");
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(p1, p2));
            }
            
            // Filter by specializations
            if(specializationIds != null && !specializationIds.isEmpty()){
                Join<Lawyer, LawyerSpecialization> specializationJoin = 
                    root.join("specializations", JoinType.INNER);
                predicate = criteriaBuilder.and(predicate,
                    specializationJoin.get("specialization").get("specId").in(specializationIds));
            }
            
            // Filter by years of experience
            if(minYearsOfExp != null){
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.greaterThanOrEqualTo(root.get("yearsOfExp"), minYearsOfExp));
            }
            
            if(maxYearsOfExp != null){
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.lessThanOrEqualTo(root.get("yearsOfExp"), maxYearsOfExp));
            }
            
            // Filter by rating (if rating field exists in Lawyer entity)
            // Note: Cần kiểm tra xem Lawyer entity có field rating không
            // if(minRating != null){
            //     predicate = criteriaBuilder.and(predicate,
            //         criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating));
            // }
            
            // Ensure distinct results when joining
            if(query != null){
                query.distinct(true);
            }

            return predicate;
        });
    }
}
