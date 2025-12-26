package com.example.backend.user.repository;

import com.example.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsUserByPhoneNumber(String phoneNumber);
    
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    Page<User> findByEmailContainingOrFullNameContaining(String email, String fullName, Pageable pageable);
    
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
}
