package com.example.userservice.user.controller;

import com.example.userservice.common.dto.ApiResponse;
import com.example.userservice.common.security.CustomUserDetails;
import com.example.userservice.common.service.UploadImageService;
import com.example.userservice.lawyer.dto.request.LawyerRequest;
import com.example.userservice.lawyer.dto.response.LawyerResponse;
import com.example.userservice.lawyer.service.LawyerService;
import com.example.userservice.user.dto.UserProfileUpdateRequest;
import com.example.userservice.user.dto.UserResponse;
import com.example.userservice.user.dto.UserSummary;
import com.example.userservice.user.entity.User;
import com.example.userservice.user.repository.UserRepository;
import com.example.userservice.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UploadImageService uploadImageService;
    private final UserRepository userRepository;
    private final LawyerService lawyerService;

    @GetMapping("/internal/ids")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByIds(@RequestParam("ids") List<Long> ids) {
        try {
            // Sử dụng findAllById để lấy dữ liệu nhanh và an toàn nhất
            List<User> users = userRepository.findAllById(ids);
            
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::from)
                    .collect(Collectors.toList());

            ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Lấy danh sách người dùng thành công")
                    .data(userResponses)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("LỖI TẠI USER-SERVICE INTERNAL API: ", e);
            return ResponseEntity.status(500).body(ApiResponse.<List<UserResponse>>builder()
                    .success(false)
                    .status(500)
                    .message("Lỗi xử lý danh sách user: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build());
        }
    }

    @GetMapping("/internal/{id}")
    public UserSummary getInternalUserById(@PathVariable Long id){
        User user = userRepository.findByUserId(id);
        List<String> roles = user.getUserRoles()
                .stream()
                .map(ur -> ur.getRole().getRoleName())
                .toList();

        return new UserSummary(id, user.getFullName(), user.getAvatarUrl(), roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Object>> getCurrentUserProfile(
            HttpServletRequest httpRequest,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long userId = user.getUser().getUserId();
        
        // Check if user is a lawyer
        boolean isLawyer = user.getUser().getUserRoles().stream()
                .anyMatch(ur -> ur.getRole() != null && 
                        "LAWYER".equalsIgnoreCase(ur.getRole().getRoleName()));
        
        Object profileData;
        if (isLawyer && user.getUser().getLawyer() != null) {
            // Return lawyer profile with full details
            profileData = lawyerService.getDetail(userId);
        } else {
            // Return basic user profile
            profileData = userService.getUserById(userId);
        }

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin hồ sơ thành công")
                .data(profileData)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody UserProfileUpdateRequest request,
            HttpServletRequest httpRequest,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long userId = user.getUser().getUserId();
        UserResponse userResponse = userService.updateProfile(userId, request);

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Cập nhật hồ sơ thành công")
                .data(userResponse)
                .timestamp(Instant.now())
                .path(httpRequest.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long userId = user.getUser().getUserId();
        String avatarUrl = userService.updateAvatar(userId, file);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Upload avatar thành công")
                .data(avatarUrl)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByfullName(
            @RequestParam String fullName,
            HttpServletRequest request
    ) {
        UserResponse userResponse = userService.searchByFullName(fullName);

        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin người dùng theo tên thành công")
                .data(userResponse)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/register-lawyer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LawyerResponse>> registerAsLawyer(
            @RequestParam("data") String data,
            @RequestParam("certificate") MultipartFile file,
            HttpServletRequest servletRequest,
            @AuthenticationPrincipal CustomUserDetails user
    ) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        LawyerRequest request = mapper.readValue(data, LawyerRequest.class);

        Long userId = user.getUser().getUserId();
        String certificateUrl = uploadImageService.uploadImage(userId, file, "certificates");
        LawyerResponse lawyerResponse = lawyerService.requestUpgrade(request, userId, certificateUrl);

        ApiResponse<LawyerResponse> response = ApiResponse.<LawyerResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Đăng ký trở thành luật sư thành công, vui lòng chờ phê duyệt")
                .data(lawyerResponse)
                .path(servletRequest.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}