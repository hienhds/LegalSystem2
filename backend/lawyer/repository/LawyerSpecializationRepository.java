package com.example.backend.lawyer.repository;

import com.example.backend.lawyer.entity.LawyerSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LawyerSpecializationRepository extends JpaRepository<LawyerSpecialization, Long> {
}
