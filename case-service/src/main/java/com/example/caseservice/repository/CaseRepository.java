package com.example.caseservice.repository;

import com.example.caseservice.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    // Tìm kiếm cho Luật sư
    @Query("SELECT c FROM Case c WHERE c.lawyerId = :lawyerId AND " +
            "(:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Case> searchCasesForLawyer(@Param("lawyerId") Long lawyerId,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    // Tìm kiếm cho Khách hàng
    @Query("SELECT c FROM Case c WHERE c.clientId = :clientId AND " +
            "(:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Case> searchCasesForClient(@Param("clientId") Long clientId,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);
}