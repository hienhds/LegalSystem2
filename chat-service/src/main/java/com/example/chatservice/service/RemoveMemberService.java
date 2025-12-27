package com.example.chatservice.service;
import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.ConversationMember;
import com.example.chatservice.event.RemoveMemberEvent;
import com.example.chatservice.exception.AppException;
import com.example.chatservice.exception.ErrorType;
import com.example.chatservice.grpc.FileServiceGrpcClient;
import com.example.chatservice.kafka.ChatEventProducer;
import com.example.chatservice.jpa.repository.BlockedUserRepository;
import com.example.chatservice.jpa.repository.ConversationInviteRepository;
import com.example.chatservice.jpa.repository.ConversationMemberRepository;
import com.example.chatservice.jpa.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RemoveMemberService {
    private final ConversationRepository conversationRepository;
    private final ConversationInviteRepository inviteRepository;
    private final ConversationMemberRepository memberRepository;
    private final ChatEventProducer chatEventProducer;
    private final BlockedUserRepository blockedUserRepository;
    private final FileServiceGrpcClient fileServiceGrpcClient;
    private final ConversationInviteService conversationInviteService;

    @Transactional
    public void removeMember(Long ownerId, Long memberId, String conversationId) {

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

        // ================= PERMISSION (OWNER) =================

        ConversationMember ownerMember = memberRepository
                .findByConversationIdAndUserId(conversationId, ownerId)
                .orElseThrow(() ->
                        new AppException(ErrorType.FORBIDDEN, "Bạn không phải là thành viên"));

        if (ownerMember.getMemberStatus() != ConversationMember.MemberStatus.OWNER) {
            throw new AppException(ErrorType.FORBIDDEN, "Chỉ OWNER mới được xoá thành viên");
        }

        if (ownerId.equals(memberId)) {
            throw new AppException(ErrorType.BAD_REQUEST, "Không thể xoá chính mình");
        }

        // ================= CHECK MEMBER EXIST =================

        ConversationMember targetMember = memberRepository
                .findByConversationIdAndUserId(conversationId, memberId)
                .orElseThrow(() ->
                        new AppException(ErrorType.NOT_FOUND, "Người dùng không phải là thành viên phòng"));

        // Chỉ cho xoá MEMBER
        if (targetMember.getMemberStatus() != ConversationMember.MemberStatus.MEMBER) {
            throw new AppException(
                    ErrorType.BAD_REQUEST,
                    "Chỉ có thể xoá thành viên có trạng thái MEMBER"
            );
        }

        // ================= REMOVE =================

        targetMember.setMemberStatus(ConversationMember.MemberStatus.REMOVED);
        targetMember.setUpdatedTime(LocalDateTime.now());
        memberRepository.save(targetMember);

        RemoveMemberEvent event = RemoveMemberEvent.builder()
                .memberId(memberId)
                .conversationName(conversation.getName())
                .timestamp(LocalDateTime.now())
                .build();

        chatEventProducer.publishRemoveMember(event);
    }

}
