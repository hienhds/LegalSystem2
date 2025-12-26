package com.example.backend.case_management.repository;

import com.example.backend.case_management.entity.Case;
import com.example.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    // Tìm vụ án của khách hàng
    Page<Case> findByClient(User client, Pageable pageable);

    // Tìm vụ án của luật sư
    Page<Case> findByLawyer(User lawyer, Pageable pageable);
}