package com.example.userservice.auth.repository;

import com.example.userservice.auth.entity.UserToken;
import com.example.userservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenHashAndAndTokenType(String tokenHash, String tokenType);

    Optional<UserToken> deleteUserTokenByUserAndAndTokenType(User user, String tokenType);
}
