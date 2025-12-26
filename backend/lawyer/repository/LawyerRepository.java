package com.example.backend.lawyer.repository;

import com.example.backend.lawyer.entity.Lawyer;
import com.example.backend.lawyer.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LawyerRepository extends JpaRepository<Lawyer, Long>, JpaSpecificationExecutor<Lawyer> {
    Long countByVerificationStatus(VerificationStatus status);
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    Page<Lawyer> findByVerificationStatus(VerificationStatus status, Pageable pageable);
    
    @Query("SELECT l FROM Lawyer l " +
           "LEFT JOIN FETCH l.user u " +
           "LEFT JOIN FETCH l.barAssociation ba " +
           "LEFT JOIN FETCH l.specializations ls " +
           "WHERE l.lawyerId = :lawyerId")
    Optional<Lawyer> findByIdWithDetails(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT AVG(a.rating) FROM Appointment a WHERE a.lawyer.lawyerId = :lawyerId AND a.rating IS NOT NULL")
    Double getAverageRating(@Param("lawyerId") Long lawyerId);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.lawyer.lawyerId = :lawyerId AND a.rating IS NOT NULL")
    Long getReviewCount(@Param("lawyerId") Long lawyerId);
}
