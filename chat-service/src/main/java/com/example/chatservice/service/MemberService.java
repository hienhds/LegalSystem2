package com.example.chatservice.service;

import com.example.chatservice.dto.response.ConversationMemberListResponse;
import com.example.chatservice.dto.response.ConversationMemberResponse;
import com.example.chatservice.exception.AppException;
import com.example.chatservice.exception.ErrorType;
import com.example.chatservice.jpa.entity.ConversationMember;
import com.example.chatservice.jpa.repository.ConversationMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private  final ConversationMemberRepository memberRepository;

    @Transactional(readOnly = true)
    public ConversationMemberListResponse getConversationMembers(
            String conversationId,
            String keyword,
            int limit,
            Long userId
    ) {

        boolean isMember = memberRepository.existsByConversation_IdAndUserIdAndMemberStatusIn(
                conversationId,
                userId,
                List.of(
                        ConversationMember.MemberStatus.OWNER,
                        ConversationMember.MemberStatus.MEMBER
                )
        );
        if (!isMember) {
            throw new AppException(
                    ErrorType.FORBIDDEN,
                    "Bạn không phải thành viên của cuộc hội thoại"
            );
        }
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        Pageable pageable = PageRequest.of(0, limit);

        List<ConversationMember> members =
                memberRepository.findMembers(
                        conversationId,
                        keyword,
                        pageable
                );

        List<ConversationMemberResponse> items = members.stream()
                .map(this::toResponse)
                .toList();

        return ConversationMemberListResponse.builder()
                .items(items)
                .build();
    }
    private ConversationMemberResponse toResponse(ConversationMember m) {
        return ConversationMemberResponse.builder()
                .userId(m.getUserId())
                .userName(m.getUserName())
                .userAvatar(m.getUserAvatar())
                .memberStatus(m.getMemberStatus())
                .build();
    }

}
