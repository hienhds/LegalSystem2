package com.example.backend.user.service;

import com.example.backend.common.exception.AppException;
import com.example.backend.common.exception.ErrorType;
import com.example.backend.common.service.UploadImageService;
import com.example.backend.lawyer.dto.response.LawyerDetailResponse;
import com.example.backend.lawyer.service.LawyerService;
import com.example.backend.user.dto.UserProfileUpdateRequest;
import com.example.backend.user.dto.UserResponse;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UploadImageService uploadImageService;
    private final LawyerService lawyerService;

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

    // Lấy profile: nếu là luật sư thì trả về LawyerDetailResponse, không thì trả về UserResponse
    public Object getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy người dùng"));

        // Kiểm tra nếu user là luật sư
        if (user.getLawyer() != null) {
            Long lawyerId = user.getLawyer().getLawyerId();
            return lawyerService.getDetail(lawyerId);
        }

        // Nếu không phải luật sư, trả về UserResponse
        return UserResponse.from(user);
    }
}
