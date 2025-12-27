package com.example.userservice.auth.service;

import com.example.userservice.auth.dto.request.RegisterRequest;
import com.example.userservice.auth.entity.UserToken;
import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.common.service.EmailService;
import com.example.userservice.user.entity.Role;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.entity.UserRole;
import com.example.userservice.user.repository.RoleRepository;
import com.example.userservice.user.repository.UserRepository;
import com.example.userservice.user.repository.UserRoleRepository;
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

            // Nếu chưa kích hoạt → gửi lại email xác nhận
            UserToken newToken = tokenService.createVerificationToken(existing);
            emailService.sendVerificationEmail(existing.getEmail(), newToken.getTokenHash());
            log.info("Resent verification email to {}", existing.getEmail());
            throw new AppException(ErrorType.CONFLICT, "Tài khoản chưa được kích hoạt. Đã gửi lại email xác nhận.");
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
        user.setActive(false); // chưa kích hoạt
        userRepository.save(user);

        // 5️⃣ Gán vai trò mặc định
        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);

        // 6️⃣ Tạo token xác minh & gửi email
        UserToken verificationToken = tokenService.createVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getTokenHash());

        log.info("User {} registered successfully. Verification email sent.", user.getEmail());
        return user;
    }
}
