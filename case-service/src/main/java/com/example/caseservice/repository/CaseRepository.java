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

    // Tìm kiếm phân trang cho người dùng (cả Luật sư và Khách hàng)
    // Lọc theo keyword trong tiêu đề hoặc mô tả
    @Query("SELECT c FROM Case c WHERE (c.lawyerId = :userId OR c.clientId = :userId) " +
            "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Case> searchMyCases(@Param("userId") Long userId,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    List<Case> findByLawyerIdOrClientId(Long lawyerId, Long clientId);
}