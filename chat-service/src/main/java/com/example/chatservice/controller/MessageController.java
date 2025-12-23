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
public class MessageController {
    private final MessageService messageService;

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
    @GetMapping("/{conversationId}/files")
    public ResponseEntity<ApiResponse<ConversationFileListResponse>> getFiles(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor,
            Authentication authentication,
            HttpServletRequest request
    ) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        ConversationFileListResponse data =
                messageService.getConversationFiles(
                        conversationId,
                        user.getUserId(),
                        limit,
                        cursor
                );

        return ResponseEntity.ok(
                ApiResponse.<ConversationFileListResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
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

    @GetMapping("/{conversationId}/messages/files/{fileId}/preview")
    public ResponseEntity<ApiResponse<PresignedFileResponse>> preview(
            @PathVariable String conversationId,
            @PathVariable String fileId,
            Authentication authentication,
            HttpServletRequest request
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        PresignedFileResponse presignedResponse =
                messageService.generateFilePreviewUrl(
                        conversationId,
                        userId,
                        fileId
                );

        ApiResponse<PresignedFileResponse> response =
                ApiResponse.<PresignedFileResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Generate presigned url thành công")
                        .data(presignedResponse)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{conversationId}/messages/files/{fileId}/download")
    public ResponseEntity<ApiResponse<PresignedFileResponse>> download(
            @PathVariable String conversationId,
            @PathVariable String fileId,
            @RequestParam String fileName,
            Authentication authentication,
            HttpServletRequest request
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        PresignedFileResponse presignedResponse =
                messageService.generateFileDownloadUrl(
                        conversationId,
                        userId,
                        fileId,
                        fileName
                );

        ApiResponse<PresignedFileResponse> response =
                ApiResponse.<PresignedFileResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Generate presigned url thành công")
                        .data(presignedResponse)
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build();

        return ResponseEntity.ok(response);
    }
}
