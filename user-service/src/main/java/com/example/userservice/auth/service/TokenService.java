package com.example.userservice.auth.service;


import com.example.userservice.auth.entity.UserToken;
import com.example.userservice.auth.repository.UserTokenRepository;
import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final UserTokenRepository userTokenRepository;

    public UserToken createVerificationToken(User user){
        userTokenRepository.deleteUserTokenByUserAndAndTokenType(user, "VERIFICATION");

        UserToken userToken = new UserToken();

        userToken.setUser(user);
        userToken.setTokenType("VERIFICATION");
        userToken.setTokenHash(UUID.randomUUID().toString());
        userToken.setCreatedAt(LocalDateTime.now());
        userToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        userToken.setUsed(false);

        userTokenRepository.save(userToken);

        return userToken;
    }

    // validate
    public UserToken validateVerificationToken(String tokenHash){
        Optional<UserToken> optionalUserToken = userTokenRepository.findByTokenHashAndAndTokenType(tokenHash, "VERIFICATION");

        if (optionalUserToken.isEmpty()) {
            throw new AppException(ErrorType.TOKEN_INVALID, "Invalid verification token");
        }
        UserToken token = optionalUserToken.get();

        if (token.isUsed()) {
            throw new AppException(ErrorType.TOKEN_USED, "Token already used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorType.TOKEN_EXPIRED, "Token expired");
        }

        // Đánh dấu đã dùng
        token.setUsed(true);
        userTokenRepository.save(token);

        return token;
    }
}
