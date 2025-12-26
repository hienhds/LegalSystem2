package com.example.backend.user.repository;

import com.example.backend.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("select r from Role r where r.roleName = :roleName")
    Optional<Role> findByRoleName(String roleName);

}
