package com.example.caseservice.repository;

import com.example.caseservice.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    // Tìm các vụ án của một luật sư
    List<Case> findByLawyerId(Long lawyerId);

    // Tìm các vụ án của một khách hàng
    List<Case> findByClientId(Long clientId);
}