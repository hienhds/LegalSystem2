package com.example.userservice.lawyer.repository;

import com.example.userservice.lawyer.entity.Lawyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LawyerRepository extends JpaRepository<Lawyer, Long>, JpaSpecificationExecutor<Lawyer> {
}
