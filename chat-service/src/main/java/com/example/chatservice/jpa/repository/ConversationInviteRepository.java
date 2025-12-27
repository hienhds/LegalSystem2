package com.example.chatservice.jpa.repository;

import com.example.chatservice.jpa.entity.ConversationInvite;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationInviteRepository extends JpaRepository<ConversationInvite, String >{
    List<ConversationInvite> findByReceiverIdAndStatus(Long receiverId, ConversationInvite.InviteStatus status);

    @Query("""
SELECT COUNT(i) > 0 FROM ConversationInvite i
WHERE i.conversation.id = :conversationId
AND i.receiverId = :userId
AND i.status = 'PENDING'
""")
    boolean existsByConversationIdAndInviteeIdAndStatusPending(
            String conversationId,
            Long userId
    );
    @Transactional
    void deleteAllByConversationId(String conversationId);

    Optional<ConversationInvite> findByIdAndReceiverId(String id, Long userId);

    long countByConversationIdAndStatus(String conversationId, ConversationInvite.InviteStatus status);

    @Query("""
SELECT i
FROM ConversationInvite i
WHERE i.receiverId = :receiverId
  AND (:status IS NULL OR i.status = :status)
  AND (
        :keyword IS NULL
        OR LOWER(i.senderName) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
  AND (
        :cursorTime IS NULL
        OR i.requestedAt < :cursorTime
        OR (i.requestedAt = :cursorTime AND i.id < :cursorId)
      )
ORDER BY i.requestedAt DESC, i.id DESC
""")
    List<ConversationInvite> findInvites(
            @Param("receiverId") Long receiverId,
            @Param("keyword") String keyword,
            @Param("status") ConversationInvite.InviteStatus status,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") String cursorId,
            Pageable pageable
    );


}
