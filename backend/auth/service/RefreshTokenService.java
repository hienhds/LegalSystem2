package com.example.backend.auth.service;


import com.example.backend.auth.entity.RefreshToken;
import com.example.backend.user.entity.User;
import com.example.backend.common.exception.AppException;
import com.example.backend.common.exception.ErrorType;
import com.example.backend.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // Thời gian sống của refresh token (14 ngày)
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 14;

    /**
     * Tạo refresh token mới cho user (dùng khi login)
     */
    public String createRefreshToken(User user, String ipAddress, String userAgent) {
        String rawToken = UUID.randomUUID().toString(); // token thực (sẽ gửi về client)
        String tokenHash = passwordEncoder.encode(rawToken); // hash để lưu trong DB (an toàn hơn)

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setIssuedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));
        token.setIpAddress(ipAddress);
        token.setUserAgent(userAgent);

        refreshTokenRepository.save(token);

        return rawToken; // trả về token thực (hash lưu DB)
    }

    /**
     * Xác thực token do client gửi lên
     */
    public RefreshToken validateRefreshToken(String rawToken) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findAll().stream()
                .filter(t -> passwordEncoder.matches(rawToken, t.getTokenHash()))
                .findFirst();

        if (tokenOpt.isEmpty()) {
            throw new AppException(ErrorType.VALIDATION_ERROR, "Refresh token không hợp lệ!");
        }

        RefreshToken token = tokenOpt.get();

        if (token.isRevoked()) {
            throw new AppException(ErrorType.TOKEN_EXPIRED, "Refresh token đã bị thu hồi!");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorType.TOKEN_EXPIRED, "Refresh token đã hết hạn!");
        }

        return token;
    }

    /**
     * Xoay vòng refresh token (cấp token mới khi gọi /refresh)
     */
    public String rotateRefreshToken(RefreshToken oldToken, String ipAddress, String userAgent) {
        // Thu hồi token cũ
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Tạo token mới
        String newToken = UUID.randomUUID().toString();
        String tokenHash = passwordEncoder.encode(newToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(oldToken.getUser());
        newRefreshToken.setTokenHash(tokenHash);
        newRefreshToken.setIssuedAt(LocalDateTime.now());
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));
        newRefreshToken.setIpAddress(ipAddress);
        newRefreshToken.setUserAgent(userAgent);

        refreshTokenRepository.save(newRefreshToken);

        // Ghi lại ID token mới vào token cũ để biết token nào thay thế nó
        oldToken.setReplacedBy(newRefreshToken.getTokenId());
        refreshTokenRepository.save(oldToken);

        return newToken;
    }

    /**
     * Thu hồi refresh token (dùng khi logout)
     */
    public void revokeRefreshToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    /**
     * Thu hồi toàn bộ token của một user (ví dụ khi đổi mật khẩu)
     */
    public void revokeAllByUser(User user) {
        refreshTokenRepository.findByUser(user).forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}
