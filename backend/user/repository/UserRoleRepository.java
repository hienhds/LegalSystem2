package com.example.backend.user.repository;

import com.example.backend.user.entity.Role;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByUserAndRole(User user, Role role);
}
