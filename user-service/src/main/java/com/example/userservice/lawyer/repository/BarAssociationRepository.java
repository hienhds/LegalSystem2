package com.example.userservice.lawyer.repository;

import com.example.userservice.lawyer.entity.BarAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarAssociationRepository extends JpaRepository<BarAssociation, Long> {
}
