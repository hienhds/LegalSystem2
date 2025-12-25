package com.example.caseservice.repository;

import com.example.caseservice.entity.CaseProgressUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseProgressUpdateRepository extends JpaRepository<CaseProgressUpdate, Long> {
}