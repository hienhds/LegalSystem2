package com.example.userservice.admin.service;

import com.example.userservice.admin.dto.UserManagementResponse;
import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserManagementResponse> getAllUsers(int page, int size, String search, Boolean status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;

        if (search != null && !search.isEmpty()) {
            users = userRepository.findByEmailContainingOrFullNameContaining(search, search, pageable);
        } else if (status != null) {
            users = userRepository.findByIsActive(status, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::convertToResponse);
    }

    @Transactional
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User not found"));
        userRepository.delete(user);
    }

    private UserManagementResponse convertToResponse(User user) {
        return UserManagementResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.isActive())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .role(user.getRoleName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
