package com.example.chatservice.jpa.repository;

import com.example.chatservice.jpa.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, String> {
    boolean existsByUserIdAndBlockedUserId(Long userId, Long blockedUserId);

    boolean existsByUserIdAndBlockedUserIdIn(Long userId, List<Long> blockedUserIds);
}
