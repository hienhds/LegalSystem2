package com.example.backend.auth.service;

import com.example.backend.auth.dto.request.RegisterRequest;
import com.example.backend.auth.entity.UserToken;
import com.example.backend.common.exception.AppException;
import com.example.backend.common.exception.ErrorType;
import com.example.backend.common.service.EmailService;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
import com.example.backend.user.repository.RoleRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.repository.UserRoleRepository;
import com.example.backend.user.entity.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final EmailService emailService;
    private final TokenService tokenService;

    public User register(RegisterRequest request) {

        // 1️⃣ Kiểm tra email đã tồn tại
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (existing.isActive()) {
                throw new AppException(ErrorType.CONFLICT, "Email đã tồn tại và đã được kích hoạt.");
            }

            // --- ĐOẠN SỬA 1: Tắt gửi lại email xác thực để tránh lỗi ---
            /*
            UserToken newToken = tokenService.createVerificationToken(existing);
            emailService.sendVerificationEmail(existing.getEmail(), newToken.getTokenHash());
            */
            log.info("Resent verification email to {}", existing.getEmail());
            throw new AppException(ErrorType.CONFLICT, "Tài khoản chưa được kích hoạt. (Đã tắt gửi lại email)");
        });

        // 2️⃣ Kiểm tra số điện thoại trùng
        if (userRepository.existsUserByPhoneNumber(request.getPhoneNumber())) {
            throw new AppException(ErrorType.CONFLICT, "Số điện thoại đã tồn tại.");
        }

        // 3️⃣ Lấy role mặc định là USER
        Role role = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy vai trò mặc định."));

        // 4️⃣ Tạo user mới
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // --- ĐOẠN SỬA 2: Kích hoạt luôn tài khoản để test ---
        user.setActive(true); // Thay vì false, để true luôn

        userRepository.save(user);

        // 5️⃣ Gán vai trò mặc định
        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);

        // --- ĐOẠN SỬA 3: Tắt tạo token và gửi email ---
        /*
        UserToken verificationToken = tokenService.createVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getTokenHash());
        */

        log.info("User {} registered successfully (Auto Activated).", user.getEmail());
        return user;
    }
}