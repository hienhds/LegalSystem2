package com.example.chatservice.service;

import com.example.chatservice.client.UserClient;
import com.example.chatservice.dto.UserSummary;
import com.example.chatservice.dto.request.ConversationRequest;
import com.example.chatservice.dto.response.ConversationListResponse;
import com.example.chatservice.dto.response.ConversationResponse;
import com.example.chatservice.dto.response.PresignedFileResponse;
import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.ConversationMember;
import com.example.chatservice.event.ConversationCreatedEvent;
import com.example.chatservice.exception.AppException;
import com.example.chatservice.exception.ErrorType;
import com.example.chatservice.grpc.FileServiceGrpcClient;
import com.example.chatservice.grpc.PresignedUrlResponse;
import com.example.chatservice.kafka.ChatEventProducer;
import com.example.chatservice.jpa.repository.BlockedUserRepository;
import com.example.chatservice.jpa.repository.ConversationInviteRepository;
import com.example.chatservice.jpa.repository.ConversationMemberRepository;
import com.example.chatservice.jpa.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationInviteRepository inviteRepository;
    private final ConversationMemberRepository memberRepository;
    private final ChatEventProducer chatEventProducer;
    private final BlockedUserRepository blockedUserRepository;
    private final FileServiceGrpcClient fileServiceGrpcClient;
    private final ConversationInviteService conversationInviteService;
    private final UserClient userClient;




    @Transactional
    public ConversationResponse createConversation(ConversationRequest conversationRequest, Long creatorUserId, String fullName, String avatar, List<String> roles){
        // ===== DIRECT VALIDATION =====
        if (conversationRequest.getType() == Conversation.ConversationType.DIRECT) {

            if (conversationRequest.getParticipantIds() == null
                    || conversationRequest.getParticipantIds().size() != 1) {
                throw new AppException(ErrorType.BAD_REQUEST, "DIRECT chỉ có 2 người");
            }

            Long targetUserId = new ArrayList<>(conversationRequest.getParticipantIds()).get(0);


            if (conversationRepository.countActiveDirectConversation(
                    creatorUserId, targetUserId
            ) == 1||
                    conversationRepository.countActiveDirectConversation(
                            targetUserId, creatorUserId
                    ) == 1) {
                throw new AppException(
                        ErrorType.CONFLICT,
                        "Cuộc trò chuyện đã tồn tại"
                );
            }
        }

        // ===== GROUP VALIDATION =====
        if (conversationRequest.getType() == Conversation.ConversationType.GROUP) {
            if (conversationRequest.getParticipantIds() == null
                    || conversationRequest.getParticipantIds().size() < 2) {
                throw new AppException(
                        ErrorType.BAD_REQUEST,
                        "Nhóm phải có ít nhất 3 người"
                );
            }
        }

        if (conversationRequest.getType() == Conversation.ConversationType.PUBLIC) {

            if (roles == null || !roles.contains("ADMIN")) {
                throw new AppException(
                        ErrorType.FORBIDDEN,
                        "Chỉ ADMIN mới được tạo PUBLIC conversation"
                );
            }

            Conversation conversation = Conversation.builder()
                    .createdByUserId(creatorUserId)
                    .ownerName(fullName)
                    .ownerAvatar(avatar)
                    .name(conversationRequest.getConversationName())
                    .type(conversationRequest.getType())
                    .note(conversationRequest.getNote())
                    .updatedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .isActive(true)
                    .build();

            conversation = conversationRepository.save(conversation);

            ConversationResponse response = ConversationResponse.builder()
                    .id(conversation.getId())
                    .name(conversation.getName())
                    .type(conversation.getType())
                    .creatorId(conversation.getCreatedByUserId())
                    .creatorFullName(fullName)
                    .creatorAvatar(avatar)
                    .build();

            return response;


        }

        Conversation conversation = Conversation.builder()
                .createdByUserId(creatorUserId)
                .ownerName(fullName)
                .ownerAvatar(avatar)
                .name(conversationRequest.getConversationName())
                .type(conversationRequest.getType())
                .note(conversationRequest.getNote())
                .updatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        conversation = conversationRepository.save(conversation);

        ConversationMember owner = ConversationMember.builder()
                .conversation(conversation)
                .memberStatus(ConversationMember.MemberStatus.OWNER)
                .userId(creatorUserId)
                .userName(fullName)
                .userAvatar(avatar)
                .build();

        owner = memberRepository.save(owner);


        // ===== CREATE INVITES =====

        for(Long memberId: conversationRequest.getParticipantIds()){
            UserSummary userSummary = userClient.getUserById(memberId);
            conversationInviteService.createdInvite(creatorUserId, fullName, avatar, userSummary, conversation);
        }

        ConversationCreatedEvent conversationCreatedEvent = ConversationCreatedEvent.builder()
                .conversationId(conversation.getId())
                .conversationName(conversation.getName())
                .creatorId(conversation.getCreatedByUserId())
                .creatorName(fullName)
                .creatorAvatar(avatar)
                .memberIds(conversationRequest.getParticipantIds())
                .createdAt(LocalDateTime.now())
                .build();

        chatEventProducer.publishGroupCreated(conversationCreatedEvent);

        ConversationResponse response = ConversationResponse.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .type(conversation.getType())
                .members(conversationRequest.getParticipantIds())
                .creatorId(conversation.getCreatedByUserId())
                .creatorFullName(fullName)
                .creatorAvatar(avatar)
                .build();

        return response;
    }


    // ============== UPLOAD AVATAR CONVERSATION ====================
    @Transactional
    public PresignedFileResponse generateAvatarUploadUrl(
            String conversationId,
            String fileName,
            String contentType,
            long fileSize,
            Long userId
    ){
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND));

        ConversationMember member = memberRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorType.FORBIDDEN));

        if (member.getMemberStatus() != ConversationMember.MemberStatus.OWNER) {
            throw new AppException(ErrorType.FORBIDDEN, "Chỉ chủ phòng được đổi avatar");
        }

        // ✅ CHỈ GROUP + ACTIVE
        if (conversation.getType() != Conversation.ConversationType.GROUP) {
            throw new AppException(ErrorType.BAD_REQUEST, "Chỉ nhóm GROUP mới được đổi avatar");
        }

        if (!conversation.isActive()) {
            throw new AppException(ErrorType.BAD_REQUEST, "Nhóm đã bị vô hiệu hóa");
        }

        PresignedUrlResponse grpcResponse = fileServiceGrpcClient.generateUploadUrl(
                fileName,
                contentType,
                fileSize,
                "GROUP_AVATAR",
                conversationId,
                userId
        );

        if (!grpcResponse.getSuccess()) {
            throw new AppException(ErrorType.INTERNAL_ERROR, grpcResponse.getErrorMessage());
        }

        return PresignedFileResponse.builder()
                .fileId(grpcResponse.getFileId())
                .uploadUrl(grpcResponse.getPresignedUrl())
                .expiredAt(grpcResponse.getExpiredAt())
                .build();
    }


    @Transactional
    public void confirmAvatarUpload(
            String conversationId,
            String fileId,
            Long userId
    ){
        ConversationMember member = memberRepository
                .findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorType.FORBIDDEN));

        if (member.getMemberStatus() != ConversationMember.MemberStatus.OWNER) {
            throw new AppException(ErrorType.FORBIDDEN);
        }

        // ✅ KHÔNG làm gì thêm
        // Chat-service chỉ xác nhận quyền
        // Việc upload thành công sẽ được xác nhận bằng Kafka event
    }

    // load danh sach conversation

    private static  record CursorData(LocalDateTime time, String id) {}
    @Transactional(readOnly = true)
    public ConversationListResponse getConversationList(
            Long userId,
            String typeString ,
            String keyword,
            int limit,
            String cursor
    ) {
        Conversation.ConversationType type = null;
        if (typeString != null && !typeString.isBlank()) {
            try {
                type = Conversation.ConversationType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                throw new AppException(
                        ErrorType.BAD_REQUEST,
                        "Conversation type không hợp lệ"
                );
            }
        }

        Pageable pageable = PageRequest.of(0, limit + 1);

        CursorData cursorData = parseCursor(cursor);

        List<Conversation> conversations =
                conversationRepository.findConversations(
                        userId,
                        type,
                        keyword,
                        cursorData == null ? null : cursorData.time(),
                        cursorData == null ? null : cursorData.id(),
                        pageable
                );

        boolean hasMore = conversations.size() > limit;
        if (hasMore) {
            conversations.remove(limit);
        }

        List<ConversationResponse> items = conversations.stream()
                .map(this::toListItem)
                .toList();

        String nextCursor = hasMore
                ? encodeCursor(conversations.get(conversations.size() - 1))
                : null;

        return ConversationListResponse.builder()
                .items(items)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }

    // =========================
    // Cursor encode / decode
    // =========================
    private CursorData parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = decoded.split("\\|");

            return new CursorData(
                    LocalDateTime.parse(parts[0]),
                    parts[1]
            );
        } catch (Exception e) {
            throw new AppException(ErrorType.BAD_REQUEST, "Cursor không hợp lệ");
        }
    }

    private String encodeCursor(Conversation c) {
        String raw = c.getLastMessageAt().toString() + "|" + c.getId();
        return Base64.getUrlEncoder().encodeToString(raw.getBytes());
    }

    // =========================
    // Mapping
    // =========================
    private ConversationResponse toListItem(Conversation c) {
        return ConversationResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType())
                .creatorId(c.getCreatedByUserId())
                .avatarUrl(c.getAvatarUrl())
                .isLocked(c.isLocked())
                .lastMessageAt(c.getLastMessageAt())
                .lastMessageText(c.getLastMessageText())
                .lastMessageSenderName(c.getLastMessageSenderName())
                .build();
    }


}
