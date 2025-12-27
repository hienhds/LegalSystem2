package com.example.chatservice.service;
import com.example.chatservice.jpa.entity.Conversation;
import com.example.chatservice.jpa.entity.ConversationMember;
import com.example.chatservice.exception.AppException;
import com.example.chatservice.exception.ErrorType;
import com.example.chatservice.kafka.ChatEventProducer;
import com.example.chatservice.jpa.repository.ConversationMemberRepository;
import com.example.chatservice.jpa.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeaveConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final ChatEventProducer chatEventProducer;

    @Transactional
    public void leaveConversation(
            String conversationId,
            Long memberId
    ) {

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

        ConversationMember member = memberRepository
                .findByConversationIdAndUserId(conversationId, memberId)
                .orElseThrow(() ->
                        new AppException(ErrorType.FORBIDDEN, "Bạn không phải là thành viên"));

        // ================= BUSINESS RULE =================

        if (member.getMemberStatus() == ConversationMember.MemberStatus.OUTED || member.getMemberStatus() == ConversationMember.MemberStatus.REMOVED) {
            throw new AppException(ErrorType.CONFLICT, "Bạn đã rời nhóm trước đó");
        }

        if (member.getMemberStatus() == ConversationMember.MemberStatus.OWNER) {
            throw new AppException(
                    ErrorType.FORBIDDEN,
                    "OWNER không thể rời nhóm. Vui lòng chuyển quyền hoặc giải tán nhóm"
            );
        }

        if (member.getMemberStatus() != ConversationMember.MemberStatus.MEMBER) {
            throw new AppException(ErrorType.FORBIDDEN, "Không thể rời nhóm");
        }

        // ================= UPDATE =================

        member.setMemberStatus(ConversationMember.MemberStatus.OUTED);
        member.setUpdatedTime(LocalDateTime.now());
        memberRepository.save(member);

        // thông báo các thành viên còn lại
    }
}
