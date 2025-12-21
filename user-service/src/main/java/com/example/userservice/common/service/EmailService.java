package com.example.userservice.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Value("${app.verification-url}")
    private String verificationUrl;

    /**
     * Gửi email đơn giản
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("✅ Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Gửi email xác thực
     */
    public void sendVerificationEmail(String to, String token) {
        String link = verificationUrl + "/api/auth/verify?token=" + token;
        String body = """
                Xin chào,

                Vui lòng nhấn vào liên kết sau để xác thực tài khoản của bạn:
                %s

                Cảm ơn bạn đã đăng ký!
                """.formatted(link);

        sendSimpleEmail(to, "Xác thực tài khoản", body);
    }
}
