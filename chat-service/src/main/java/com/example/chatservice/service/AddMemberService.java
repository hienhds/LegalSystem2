package com.example.chatservice.service;

import com.example.chatservice.dto.UserSummary;
import com.example.chatservice.dto.request.AddMemberRequest;
import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.ConversationMember;
import com.example.chatservice.exception.AppException;
import com.example.chatservice.exception.ErrorType;
import com.example.chatservice.jpa.repository.BlockedUserRepository;
import com.example.chatservice.jpa.repository.ConversationInviteRepository;
import com.example.chatservice.jpa.repository.ConversationMemberRepository;
import com.example.chatservice.jpa.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddMemberService {

    private final ConversationRepository conversationRepository;
    private final ConversationInviteRepository inviteRepository;
    private final ConversationMemberRepository memberRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final ConversationInviteService conversationInviteService;

    @Transactional
    public void addMember(String conversationId, AddMemberRequest request, Long ownerId, String fullName, String avatar) {

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new AppException(ErrorType.NOT_FOUND, "Conversation không tồn tại"));

        if (conversation.getType() != Conversation.ConversationType.GROUP) {
            throw new AppException(ErrorType.BAD_REQUEST, "Chỉ áp dụng cho GROUP");
        }

        if (!conversation.isActive()) {
            throw new AppException(ErrorType.BAD_REQUEST, "Conversation chưa được kích hoạt");
        }

        if (conversation.isLocked()) {
            throw new AppException(ErrorType.FORBIDDEN, "Conversation đang bị khoá");
        }

        // ================= PERMISSION =================

        ConversationMember ownerMember = memberRepository
                .findByConversationIdAndUserId(conversationId, ownerId)
                .orElseThrow(() ->
                        new AppException(ErrorType.FORBIDDEN, "Bạn không phải là thành viên"));

        if (ownerMember.getMemberStatus() != ConversationMember.MemberStatus.OWNER) {
            throw new AppException(ErrorType.FORBIDDEN, "Chỉ OWNER mới được thêm thành viên");
        }

        if (ownerMember.getMemberStatus() == ConversationMember.MemberStatus.OUTED) {
            throw new AppException(ErrorType.FORBIDDEN, "Bạn không còn quyền trong nhóm");
        }

        if (ownerId.equals(request.getUserId())) {
            throw new AppException(ErrorType.BAD_REQUEST, "Không thể thêm chính mình");
        }

        // ================= MEMBER EXIST =================

        memberRepository.findByConversationIdAndUserId(conversationId, request.getUserId())
                .ifPresent(member -> {
                    if (member.getMemberStatus() == ConversationMember.MemberStatus.MEMBER) {
                        throw new AppException(
                                ErrorType.CONFLICT,
                                "User đã là thành viên của nhóm"
                        );
                    }
                });

        // ================= BLOCK CHECK =================

        boolean isBlocked =
                blockedUserRepository.existsByUserIdAndBlockedUserId(ownerId, request.getUserId())
                        || blockedUserRepository.existsByUserIdAndBlockedUserId(request.getUserId(), ownerId);

        if (isBlocked) {
            throw new AppException(
                    ErrorType.FORBIDDEN,
                    "Không thể thêm user do bị block"
            );
        }

        // ================= PENDING INVITE =================

        if (inviteRepository.existsByConversationIdAndInviteeIdAndStatusPending(
                conversationId, request.getUserId())) {
            throw new AppException(
                    ErrorType.CONFLICT,
                    "User đã có lời mời đang chờ"
            );
        }

        // ================= CREATE INVITE =================

        UserSummary userSummary = UserSummary.builder()
                .id(request.getUserId())
                .avatar(request.getAvatar())
                .fullName(request.getUserFullName())
                .build();
        conversationInviteService.createdInvite(ownerId, fullName, avatar, userSummary, conversation);
    }
}
