package com.example.chatservice.jpa.repository;

import com.example.chatservice.jpa.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    @Query(value = """
SELECT COUNT(*) 
FROM conversations c
WHERE c.type = 'DIRECT'
  AND c.is_active = true
  AND c.is_locked = false
  AND EXISTS (
      SELECT 1 FROM conversation_members m1 
      WHERE m1.conversation_id = c.id AND m1.user_id = :userA
  )
  AND EXISTS (
      SELECT 1 FROM conversation_members m2 
      WHERE m2.conversation_id = c.id AND m2.user_id = :userB
  )
""", nativeQuery = true)
    Long countActiveDirectConversation(@Param("userA") Long userA, @Param("userB") Long userB);



    @Query("""
    SELECT DISTINCT c
    FROM Conversation c
    JOIN c.members m
    WHERE m.userId = :userId
      AND (:type IS NULL OR c.type = :type)
      AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (
            :cursorTime IS NULL
            OR c.lastMessageAt < :cursorTime
            OR (c.lastMessageAt = :cursorTime AND c.id < :cursorId)
          )
    ORDER BY c.lastMessageAt DESC, c.id DESC
    """)
    List<Conversation> findConversations(
            @Param("userId") Long userId,
            @Param("type") Conversation.ConversationType type,
            @Param("keyword") String keyword,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") String cursorId,
            Pageable pageable
    );
}

