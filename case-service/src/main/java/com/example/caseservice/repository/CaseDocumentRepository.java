package com.example.caseservice.repository;

import com.example.caseservice.entity.CaseDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseDocumentRepository extends JpaRepository<CaseDocument, Long> {
}