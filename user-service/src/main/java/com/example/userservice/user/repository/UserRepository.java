package com.example.userservice.user.repository;

import com.example.userservice.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByFullName(String fullName);

    boolean existsUserByPhoneNumber(String phoneNumber);

    @Query("select u.userId, u.fullName, u.isLawyer from User u where u.userId in :ids")
    List<Object[]> findIdNameIsLawyerByIds(java.util.Set<Long> ids);

    List<User> findByUserIdIn(Set<Long> ids);

    User findByUserId(Long id);
    
    // For admin dashboard
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // For user management
    Page<User> findByEmailContainingOrFullNameContaining(String email, String fullName, Pageable pageable);
    
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
}
