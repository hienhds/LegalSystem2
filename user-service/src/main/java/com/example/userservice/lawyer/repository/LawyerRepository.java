package com.example.userservice.lawyer.repository;

import com.example.userservice.lawyer.entity.Lawyer;
import com.example.userservice.lawyer.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LawyerRepository extends JpaRepository<Lawyer, Long>, JpaSpecificationExecutor<Lawyer> {
    
    // For admin dashboard statistics
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    Long countByVerificationStatus(VerificationStatus status);
}
