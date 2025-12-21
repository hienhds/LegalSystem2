package com.example.chatservice.controller;
import com.example.chatservice.dto.request.ConversationRequest;
import com.example.chatservice.dto.response.ApiResponse;
import com.example.chatservice.dto.response.ConversationResponse;
import com.example.chatservice.dto.response.InviteListResponse;
import com.example.chatservice.dto.response.PresignedAvatarResponse;
import com.example.chatservice.middleware.UserPrincipal;
import com.example.chatservice.service.ConversationInviteService;
import com.example.chatservice.service.ConversationService;
import org.springframework.security.oauth2.jwt.Jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room")

public class ConversationInviteController {
    private final ConversationInviteService inviteService;

    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvite(
            @PathVariable String inviteId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ){

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long userId = userPrincipal.getUserId();
        String fullName = userPrincipal.getFullName();
        String avatar = userPrincipal.getAvatar();

        inviteService.acceptInvite(inviteId, userId, fullName, avatar);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Bạn đã tham gia nhom")
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }
    @PostMapping("/invites/{inviteId}/decline")
    public ResponseEntity<ApiResponse<Void>> rejectInvite(
            @PathVariable String inviteId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long userId = userPrincipal.getUserId();
        String fullName = userPrincipal.getFullName();
        String avatar = userPrincipal.getAvatar();
        inviteService.declineInvite(inviteId, userId, fullName, avatar);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Bạn đã tu choi tham gia nhom")
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @GetMapping("/invites")
    public ResponseEntity<ApiResponse<InviteListResponse>> getInvites(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor,
            Authentication authentication,
            HttpServletRequest request
    ) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        InviteListResponse data = inviteService.getInviteList(
                user.getUserId(),
                keyword,
                status,
                limit,
                cursor
        );

        return ResponseEntity.ok(
                ApiResponse.<InviteListResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách lời mời thành công")
                        .data(data)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

}
