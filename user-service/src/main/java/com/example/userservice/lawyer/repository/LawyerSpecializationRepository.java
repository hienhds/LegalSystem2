package com.example.userservice.lawyer.repository;

import com.example.userservice.lawyer.entity.LawyerSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LawyerSpecializationRepository extends JpaRepository<LawyerSpecialization, Long> {
}
