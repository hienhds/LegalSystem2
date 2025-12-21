package com.example.chatservice.service;

import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.ConversationMember;
import com.example.chatservice.event.DissolveGroupEvent;
import com.example.chatservice.exception.AppException;
import com.example.chatservice.exception.ErrorType;
import com.example.chatservice.grpc.FileServiceGrpcClient;
import com.example.chatservice.kafka.ChatEventProducer;
import com.example.chatservice.jpa.repository.ConversationInviteRepository;
import com.example.chatservice.jpa.repository.ConversationMemberRepository;
import com.example.chatservice.jpa.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DissolveConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationInviteRepository inviteRepository;
    private final ChatEventProducer chatEventProducer;
    private final FileServiceGrpcClient fileServiceGrpcClient;

    @Transactional
    public void dissolveGroup(String conversationId, Long ownerId) {

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new AppException(ErrorType.NOT_FOUND, "Conversation không tồn tại"));

        if (conversation.getType() != Conversation.ConversationType.GROUP) {
            throw new AppException(ErrorType.BAD_REQUEST, "Chỉ áp dụng cho GROUP");
        }

        if (!conversation.isActive()) {
            throw new AppException(ErrorType.BAD_REQUEST, "Conversation đã bị giải tán");
        }

        if (conversation.isLocked()) {
            throw new AppException(ErrorType.FORBIDDEN, "Conversation đang bị khoá");
        }

        ConversationMember ownerMember = memberRepository
                .findByConversationIdAndUserId(conversationId, ownerId)
                .orElseThrow(() ->
                        new AppException(ErrorType.FORBIDDEN, "Bạn không phải là thành viên"));

        if (ownerMember.getMemberStatus() != ConversationMember.MemberStatus.OWNER) {
            throw new AppException(ErrorType.FORBIDDEN, "Chỉ OWNER mới được giải tán nhóm");
        }

        // ================= 1. Deactivate conversation =================

        conversation.setLocked(true);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // ================= 2. Out tất cả member =================

        List<ConversationMember> members =
                memberRepository.findAllByConversationId(conversationId);

        List<Long> memberIds = new ArrayList<>();
        for (ConversationMember member : members) {
            memberIds.add(member.getUserId());
            member.setMemberStatus(ConversationMember.MemberStatus.OUTED);
            member.setUpdatedTime(LocalDateTime.now());
        }
        memberRepository.saveAll(members);

        // ================= 3. Delete pending invites =================

        inviteRepository.deleteAllByConversationId(conversationId);

        // ================= 4. Remove avatar (optional) =================
        // if (conversation.getAvatarUrl() != null) {
        //     fileServiceGrpcClient.deleteGroupAvatar(conversationId);
        // }

        // ================= 5. Publish event =================

        chatEventProducer.publishDissolveGroup(
                DissolveGroupEvent.builder()
                        .memberIds(memberIds)
                        .conversationName(conversation.getName())
                        .ownerId(ownerId)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
