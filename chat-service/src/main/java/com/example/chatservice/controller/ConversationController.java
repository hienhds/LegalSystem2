package com.example.chatservice.controller;

import com.example.chatservice.dto.request.ConversationRequest;
import com.example.chatservice.dto.response.ApiResponse;
import com.example.chatservice.dto.response.ConversationListResponse;
import com.example.chatservice.dto.response.ConversationResponse;
import com.example.chatservice.dto.response.PresignedAvatarResponse;
import com.example.chatservice.middleware.UserPrincipal;
import com.example.chatservice.service.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    // ================= CREATE CONVERSATION =================

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @RequestBody ConversationRequest conversationRequest,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {


        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long senderId = userPrincipal.getUserId();
        String fullName = userPrincipal.getFullName();
        String avatar = userPrincipal.getAvatar();

        System.out.println("=======================================================");
        System.out.println("userId: " + senderId);
        System.out.println("fullName: " + fullName);
        System.out.println("=======================================================");

        ConversationResponse conversationResponse =
                conversationService.createConversation(conversationRequest, senderId, fullName, avatar);

        ApiResponse<ConversationResponse> response =
                ApiResponse.<ConversationResponse>builder()
                        .success(true)
                        .status(HttpStatus.CREATED.value())
                        .message("Tạo phòng thành công")
                        .data(conversationResponse)
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ================= GENERATE PRESIGNED AVATAR URL =================

    @PostMapping("/{conversationId}/avatar")
    public ResponseEntity<ApiResponse<PresignedAvatarResponse>> generateAvatarPresignedUrl(
            @PathVariable String conversationId,
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam long fileSize,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long userId = userPrincipal.getUserId();
        String fullName = userPrincipal.getFullName();
        String avatar = userPrincipal.getAvatar();

        PresignedAvatarResponse presignedResponse =
                conversationService.generateAvatarUploadUrl(
                        conversationId,
                        fileName,
                        contentType,
                        fileSize,
                        userId
                );

        ApiResponse<PresignedAvatarResponse> response =
                ApiResponse.<PresignedAvatarResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Generate presigned url thành công")
                        .data(presignedResponse)
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    // lay danh sach cac cuoc hoi thoai
    // ================= GET CONVERSATION LIST =================

    @GetMapping
    public ResponseEntity<ApiResponse<ConversationListResponse>> getConversations(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor,
            HttpServletRequest request,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long userId = userPrincipal.getUserId();
        String fullName = userPrincipal.getFullName();
        String avatar = userPrincipal.getAvatar();

        ConversationListResponse data =
                conversationService.getConversationList(
                        userId, type, keyword, limit, cursor
                );

        return ResponseEntity.ok(
                ApiResponse.<ConversationListResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách hội thoại thành công")
                        .data(data)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

}
