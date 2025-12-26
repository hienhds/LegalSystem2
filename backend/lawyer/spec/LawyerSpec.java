package com.example.backend.lawyer.spec;

import com.example.backend.lawyer.entity.Lawyer;
import com.example.backend.lawyer.entity.LawyerSpecialization;
import com.example.backend.lawyer.entity.Specialization;
import com.example.backend.lawyer.entity.VerificationStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.List;

public class LawyerSpec {
    public static Specification<Lawyer> filter(
            VerificationStatus verificationStatus,
            Long barAssociationId,
            String keyword,
            List<Long> specializationIds,
            Integer minYearsOfExp,
            Integer maxYearsOfExp
    ){
        return ((root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            
            if(verificationStatus != null){
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(root.get("verificationStatus"), verificationStatus));
            }

            if(barAssociationId != null){
                predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.equal(root.get("barAssociation").get("barAssociationId"), barAssociationId));
            }

            if(keyword != null && !keyword.isEmpty() && !keyword.isBlank()){
                Predicate p1 = criteriaBuilder.like(root.get("barLicenseId"), "%" + keyword + "%");
                Predicate p2 = criteriaBuilder.like(root.get("user").get("fullName"), "%" + keyword + "%");
                Predicate p3 = criteriaBuilder.like(root.get("user").get("email"), "%" + keyword + "%");
                Predicate p4 = criteriaBuilder.like(root.get("bio"), "%" + keyword + "%");
                Predicate p5 = criteriaBuilder.like(root.get("user").get("phoneNumber"), "%" + keyword + "%");

                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(p1, p2, p3, p4, p5));
            }

            // Filter by specializations
            if(specializationIds != null && !specializationIds.isEmpty()){
                Join<Lawyer, LawyerSpecialization> specJoin = root.join("specializations");
                predicate = criteriaBuilder.and(predicate,
                    specJoin.get("specialization").get("specId").in(specializationIds));
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

            // Ensure distinct results when joining with specializations
            if(query != null){
                query.distinct(true);
            }

            return predicate;
        });
    }
}
