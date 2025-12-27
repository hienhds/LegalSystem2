package com.example.userservice.auth.service;


import com.example.userservice.auth.entity.PasswordResetToken;
import com.example.userservice.auth.repository.PasswordResetTokenRepository;
import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.common.service.EmailService;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final long TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 phút

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Gửi liên kết reset password thật (backend link)
     */
    public void sendResetPasswordLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy người dùng với email này."));

        if (!user.isActive()) {
            throw new AppException(ErrorType.FORBIDDEN, "Tài khoản chưa được kích hoạt.");
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .used(false)
                .expiredAt(Instant.now().plusMillis(TOKEN_EXPIRATION_MS))
                .user(user)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String link = backendUrl + "/api/auth/reset-password/validate?token=" + token;

        String body = """
                Xin chào %s,

                Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản của mình.
                Nhấn vào liên kết dưới đây để xác thực và đặt lại mật khẩu:
                %s

                Liên kết này sẽ hết hạn sau 15 phút.
                """.formatted(user.getFullName(), link);

        emailService.sendSimpleEmail(user.getEmail(), "Yêu cầu đặt lại mật khẩu", body);
    }

    /**
     * Xác thực token & redirect sang frontend
     */
    public String validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorType.UNAUTHORIZED, "Token không hợp lệ."));

        if (resetToken.isUsed() || resetToken.getExpiredAt().isBefore(Instant.now())) {
            throw new AppException(ErrorType.UNAUTHORIZED, "Token đã hết hạn hoặc không hợp lệ.");
        }

        // Token hợp lệ → redirect sang trang frontend reset password
        return frontendUrl + "/reset-password?token=" + token;
    }

    /**
     * Đặt lại mật khẩu
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorType.UNAUTHORIZED, "Token không hợp lệ."));

        if (resetToken.isUsed() || resetToken.getExpiredAt().isBefore(Instant.now())) {
            throw new AppException(ErrorType.UNAUTHORIZED, "Token đã hết hạn hoặc không hợp lệ.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        resetToken.setUsed(true);

        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
    }
}
