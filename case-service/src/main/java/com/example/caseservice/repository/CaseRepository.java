package com.example.caseservice.repository;

import com.example.caseservice.entity.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    @Query("SELECT c FROM Case c WHERE c.lawyerId = :lawyerId AND " +
            "(:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Case> searchCasesForLawyer(@Param("lawyerId") Long lawyerId,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    @Query("SELECT c FROM Case c WHERE c.clientId = :clientId AND " +
            "(:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Case> searchCasesForClient(@Param("clientId") Long clientId,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    // Lấy tất cả vụ án để lọc nâng cao trong Java
    List<Case> findAllByLawyerId(Long lawyerId);
    List<Case> findAllByClientId(Long clientId);
}