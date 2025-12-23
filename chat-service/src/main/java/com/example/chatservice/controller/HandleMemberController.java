package com.example.chatservice.controller;
import com.example.chatservice.dto.request.AddMemberRequest;
import com.example.chatservice.dto.request.SendFileMessageRequest;
import com.example.chatservice.dto.request.SendTextMessageRequest;
import com.example.chatservice.dto.response.*;
import com.example.chatservice.middleware.UserPrincipal;
import com.example.chatservice.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room/conversations")
public class HandleMemberController {

    private final AddMemberService addMemberService;
    private final RemoveMemberService removeMemberService;
    private final MemberService memberService;
    private final MessageService messageService;

    // =========== load tin nhan =============
    // =========== LOAD MESSAGES ===========
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageListResponse>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor,
            HttpServletRequest request,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // (Optional nhưng KHUYẾN NGHỊ)
        // Có thể check user có phải member không ở service
        MessageListResponse data = messageService.getMessages(
                conversationId,
                limit,
                cursor
        );

        return ResponseEntity.ok(
                ApiResponse.<MessageListResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách tin nhắn thành công")
                        .data(data)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }




    // ================= ADD MEMBER =================

    @PostMapping("/{conversationId}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable String conversationId,
            @RequestBody AddMemberRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long ownerId = userPrincipal.getUserId();
        String fullName = userPrincipal.getFullName();
        String avatar = userPrincipal.getAvatar();


        addMemberService.addMember(conversationId, request, ownerId, fullName, avatar);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Đã gửi lời mời tham gia nhóm")
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{conversationId}/members")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable String conversationId,
            @RequestBody AddMemberRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long ownerId = userPrincipal.getUserId();
        String fullName = userPrincipal.getFullName();
        String avatar = userPrincipal.getAvatar();

        removeMemberService.removeMember(ownerId, request.getUserId(), conversationId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Đã xoá thành viên khỏi nhóm")
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    private final LeaveConversationService leaveConversationService;

    @PostMapping("/{conversationId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @PathVariable String conversationId,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long userId = userPrincipal.getUserId();
        String fullName = userPrincipal.getFullName();
        String avatar = userPrincipal.getAvatar();

        leaveConversationService.leaveConversation(conversationId, userId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Bạn đã rời khỏi nhóm")
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<ApiResponse<ConversationMemberListResponse>> getMembers(
            @PathVariable String conversationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication,
            HttpServletRequest request
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Long userId = userPrincipal.getUserId();

        ConversationMemberListResponse data =
                memberService.getConversationMembers(
                        conversationId,
                        keyword,
                        limit, userId
                );

        return ResponseEntity.ok(
                ApiResponse.<ConversationMemberListResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách thành viên thành công")
                        .data(data)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @PostMapping("/{conversationId}/messages/text")
    public ResponseEntity<ApiResponse<MessageResponse>> sendTextMessage(
            @PathVariable String conversationId,
            @RequestBody SendTextMessageRequest request,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        MessageResponse data = messageService.sendTextMessage(
                conversationId,
                userPrincipal.getUserId(),
                userPrincipal.getFullName(),
                userPrincipal.getAvatar(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<MessageResponse>builder()
                        .success(true)
                        .status(HttpStatus.CREATED.value())
                        .message("Gửi tin nhắn thành công")
                        .data(data)
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @PostMapping("/{conversationId}/messages/file")
    public ResponseEntity<ApiResponse<PresignedFileResponse>> generateFileUrl(
            @PathVariable String conversationId,
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam long fileSize,
            HttpServletRequest servletRequest,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        PresignedFileResponse presignedResponse =
                messageService.generateFileUploadUrl(
                        conversationId,
                        userId,
                        fileName,
                        contentType,
                        fileSize
                );
        ApiResponse<PresignedFileResponse> response =
                ApiResponse.<PresignedFileResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Generate presigned url thành công")
                        .data(presignedResponse)
                        .path(servletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
}