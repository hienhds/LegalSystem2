package com.example.chatservice.jpa.repository;

import com.example.chatservice.jpa.entity.ConversationMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, String> {
    Optional<ConversationMember> findByConversationIdAndUserId(String conversationId, Long userId);

    boolean existsByConversationIdAndUserId(
            String conversationId,
            Long userId
    );
    List<ConversationMember> findAllByConversationId(
            String conversationId
    );

    long countByConversationId(String conversationId);

    @Query("""
    SELECT m
    FROM ConversationMember m
    WHERE m.conversation.id = :conversationId
      AND m.memberStatus IN (
        com.example.chatservice.jpa.entity.ConversationMember.MemberStatus.OWNER,
        com.example.chatservice.jpa.entity.ConversationMember.MemberStatus.MEMBER
      )
      AND (
            :keyword IS NULL
            OR LOWER(m.userName) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    ORDER BY
        CASE m.memberStatus
            WHEN com.example.chatservice.jpa.entity.ConversationMember.MemberStatus.OWNER THEN 0
            WHEN com.example.chatservice.jpa.entity.ConversationMember.MemberStatus.MEMBER THEN 1
        END,
        m.userName ASC
    """)


    List<ConversationMember> findMembers(
                @Param("conversationId") String conversationId,
                @Param("keyword") String keyword,
                Pageable pageable
        );

    boolean existsByConversation_IdAndUserIdAndMemberStatusIn(
            String conversationId,
            Long userId,
            Collection<ConversationMember.MemberStatus> statuses
    );

}
