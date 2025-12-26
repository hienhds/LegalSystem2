package com.example.userservice.user.service;

import com.example.userservice.common.exception.AppException;
import com.example.userservice.common.exception.ErrorType;
import com.example.userservice.common.service.UploadImageService;
import com.example.userservice.user.dto.UserProfileUpdateRequest;
import com.example.userservice.user.dto.UserResponse;
import com.example.userservice.user.dto.UserSummary;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UploadImageService uploadImageService;

    // Lấy user và trả về DTO
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy người dùng"));

        return UserResponse.from(user);
    }

    // Update profile (chỉ cập nhật các trường text)
    public UserResponse updateProfile(Long userId, UserProfileUpdateRequest request) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User không tồn tại"));

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);

        return UserResponse.from(user);
    }

    // Upload avatar
    public String updateAvatar(Long userId, MultipartFile file) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User không tồn tại"));

        // Upload file
        String avatarUrl = uploadImageService.uploadImage(userId, file, "avatar");

        // Update DB
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    public UserResponse searchByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User không tồn tại"));

        UserResponse userResponse = UserResponse.builder()
                .userId(user.getUserId())
                .email(email)
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .phoneNumber(user.getPhoneNumber())
                .build();

        return userResponse;
    }

    public UserResponse searchByFullName(String fullName){
        User user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "User không tồn tại"));

        UserResponse userResponse = UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .phoneNumber(user.getPhoneNumber())
                .build();

        return userResponse;
    }

}
