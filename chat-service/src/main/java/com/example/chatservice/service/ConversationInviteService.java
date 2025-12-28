package com.example.chatservice.service;

import com.example.chatservice.dto.UserSummary;
import com.example.chatservice.dto.response.ConversationInviteResponse;
import com.example.chatservice.dto.response.InviteListResponse;
import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.ConversationInvite;
import com.example.chatservice.jpa.entity.ConversationMember;
import com.example.chatservice.event.ConversationInviteDeclinedEvent;
import com.example.chatservice.event.ConversationJoinedEvent;
import com.example.chatservice.event.InviteEvent;
import com.example.chatservice.exception.AppException;
import com.example.chatservice.exception.ErrorType;
import com.example.chatservice.grpc.FileServiceGrpcClient;
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
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationInviteService {
    private final ConversationRepository conversationRepository;
    private final ConversationInviteRepository inviteRepository;
    private final ConversationMemberRepository memberRepository;
    private final ChatEventProducer chatEventProducer;
    private final BlockedUserRepository blockedUserRepository;
    private final FileServiceGrpcClient fileServiceGrpcClient;

    @Transactional
    public void createdInvite(Long senderId, String fullName, String avatar, UserSummary userSummary, Conversation conversation){
        boolean blockedByCreator =
                blockedUserRepository.existsByUserIdAndBlockedUserId(senderId, userSummary.getId());

        boolean blockedByMember =
                blockedUserRepository.existsByUserIdAndBlockedUserId(userSummary.getId(), senderId);

        if (blockedByCreator || blockedByMember) {
            throw new AppException(
                    ErrorType.FORBIDDEN,
                    "Không thể mời người dùng đã block hoặc bị block"
            );
        }
//        ConversationMember member = ConversationMember.builder()
//                .memberStatus(ConversationMember.MemberStatus.PENDING)
//                .conversation(conversation)
//                .userId(receiveId)
//                .build();
//
//        member = memberRepository.save(member);

        ConversationInvite invite = ConversationInvite.builder()
                .conversation(conversation)
                .status(ConversationInvite.InviteStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .senderId(senderId)
                .senderName(fullName)
                .senderAvatar(avatar)
                .receiverId(userSummary.getId())
                .receiverName(userSummary.getFullName())
                .receiverAvatar(userSummary.getAvatar())
                .build();

        invite = inviteRepository.save(invite);

        InviteEvent inviteEvent = InviteEvent.builder()
                .conversationName(conversation.getName())
                .timestamp(LocalDateTime.now())
                .senderId(senderId)
                .senderName(fullName)
                .senderAvatar(avatar)
                .receiverId(userSummary.getId())
                .receiverName(userSummary.getFullName())
                .receiverAvatar(userSummary.getAvatar())
                .type(conversation.getType()+"")
                .note(conversation.getNote())
                .build();

        chatEventProducer.publishInviteCreated(inviteEvent);
    }


    @Transactional
    public void acceptInvite(String invitedId, Long userId, String fullName, String avatar){
        ConversationInvite invite = inviteRepository
                .findByIdAndReceiverId(invitedId, userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lời mời"));

        if (invite.getStatus() != ConversationInvite.InviteStatus.PENDING) {
            throw new AppException(ErrorType.BAD_REQUEST, "Lời mời đã được xử lý");
        }

        Conversation conversation = invite.getConversation();

        if (!memberRepository.existsByConversationIdAndUserId(conversation.getId(), userId)) {
            ConversationMember member = ConversationMember.builder()
                    .conversation(conversation)
                    .userId(userId)
                    .userName(fullName)
                    .userAvatar(avatar)
                    .memberStatus(ConversationMember.MemberStatus.MEMBER)
                    .build();
            memberRepository.save(member);
        }

        invite.setStatus(ConversationInvite.InviteStatus.ACCEPTED);
        inviteRepository.save(invite);

        if(conversation.isActive() == false){
            conversation.setActive(true);
            conversationRepository.save(conversation);
        }

        ConversationJoinedEvent event = ConversationJoinedEvent.builder()
                .conversationId(conversation.getId())
                .conversationName(conversation.getName())
                .type(conversation.getType())
                .userId(userId)
                .joinedAt(LocalDateTime.now())
                .build();

        chatEventProducer.publishConversationJoined(event);
    }


    // reject

    @Transactional
    public void declineInvite(String inviteId, Long userId, String fullName, String avatar){
        ConversationInvite invite = inviteRepository
                .findByIdAndReceiverId(inviteId, userId)
                .orElseThrow(() -> new AppException(ErrorType.NOT_FOUND, "Không tìm thấy lời mời"));

        if (invite.getStatus() != ConversationInvite.InviteStatus.PENDING) {
            throw new AppException(ErrorType.BAD_REQUEST, "Lời mời đã được xử lý");
        }
        Conversation conversation = invite.getConversation();
        invite.setStatus(ConversationInvite.InviteStatus.REJECTED);
        inviteRepository.save(invite);

        ConversationInviteDeclinedEvent event = ConversationInviteDeclinedEvent.builder()
                .inviteId(inviteId)
                .conversationName(conversation.getName())
                .ownerId(conversation.getCreatedByUserId())
                .userId(userId)
                .userFullName(fullName)
                .userAvatar(avatar)
                .declineAt(LocalDateTime.now())
                .build();

        chatEventProducer.publishDeclineInvite(event);
    }


    private static record CursorData(
            LocalDateTime time,
            String id
    ) {}

    @Transactional(readOnly = true)
    public InviteListResponse getInviteList(
            Long receiverId,
            String keyword,
            String statusString,
            int limit,
            String cursor
    ) {
        // keyword normalize
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        // parse status
        ConversationInvite.InviteStatus status = null;
        if (statusString != null && !statusString.isBlank()) {
            try {
                status = ConversationInvite.InviteStatus.valueOf(statusString);
            } catch (IllegalArgumentException e) {
                throw new AppException(
                        ErrorType.BAD_REQUEST,
                        "Invite status không hợp lệ"
                );
            }
        }

        Pageable pageable = PageRequest.of(0, limit + 1);
        CursorData cursorData = parseCursor(cursor);

        List<ConversationInvite> invites =
                inviteRepository.findInvites(
                        receiverId,
                        keyword,
                        status,
                        cursorData == null ? null : cursorData.time(),
                        cursorData == null ? null : cursorData.id(),
                        pageable
                );

        boolean hasMore = invites.size() > limit;
        if (hasMore) {
            invites.remove(limit);
        }

        List<ConversationInviteResponse> items = invites.stream()
                .map(this::toResponse)
                .toList();

        String nextCursor = hasMore
                ? encodeCursor(invites.get(invites.size() - 1))
                : null;

        return InviteListResponse.builder()
                .items(items)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }

    private CursorData parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = decoded.split("\\|");

            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }

            return new CursorData(
                    LocalDateTime.parse(parts[0]),
                    parts[1]
            );
        } catch (Exception e) {
            throw new AppException(ErrorType.BAD_REQUEST, "Cursor không hợp lệ");
        }
    }

    private String encodeCursor(ConversationInvite i) {
        String raw = i.getRequestedAt() + "|" + i.getId();
        return Base64.getUrlEncoder().encodeToString(raw.getBytes());
    }

    private ConversationInviteResponse toResponse(ConversationInvite i) {
        return ConversationInviteResponse.builder()
                .id(i.getId())
                .conversationId(i.getConversation().getId())
                .receiverId(i.getReceiverId())
                .receiverName(i.getReceiverName())
                .receiverAvatar(i.getReceiverAvatar())
                .senderId(i.getSenderId())
                .senderName(i.getSenderName())
                .senderAvatar(i.getSenderAvatar())
                .status(i.getStatus())
                .requestedAt(i.getRequestedAt())
                .respondedAt(i.getRespondedAt())
                .build();
    }



}
