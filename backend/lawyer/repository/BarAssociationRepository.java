package com.example.backend.lawyer.repository;

import com.example.backend.lawyer.entity.BarAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarAssociationRepository extends JpaRepository<BarAssociation, Long> {
}
