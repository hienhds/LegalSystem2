package com.example.chatservice.mongo.repository;

import com.example.chatservice.mongo.document.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository
        extends MongoRepository<Message, String> {

    // ================= LOAD HISTORY =================

    List<Message> findByConversationIdOrderByTimestampDesc(
            String conversationId,
            Pageable pageable
    );

    // ================= LOAD BY TYPE =================

    List<Message> findByConversationIdAndTypeOrderByTimestampDesc(
            String conversationId,
            Message.MessageType type,
            Pageable pageable
    );

    // ================= SEARCH TEXT =================

    List<Message> findByConversationIdAndTypeAndContentContainingIgnoreCase(
            String conversationId,
            Message.MessageType type,
            String keyword,
            Pageable pageable
    );

    // ================= CURSOR PAGINATION =================
    // Load các message cũ hơn (timestamp DESC, _id DESC)

    @Query("""
    {
      conversationId: ?0,
      $or: [
        { timestamp: { $lt: ?1 } },
        {
          $and: [
            { timestamp: ?1 },
            { _id: { $lt: ?2 } }
          ]
        }
      ]
    }
    """)
    List<Message> findOlderMessages(
            String conversationId,
            LocalDateTime cursorTime,
            String cursorId,
            Pageable pageable
    );

    @Query("""
        {
          'conversationId': ?0,
          'type': ?1,
          '$or': [
            { 'timestamp': { '$lt': ?2 } },
            {
              'timestamp': ?2,
              '_id': { '$lt': ?3 }
            }
          ]
        }
        """)
    List<Message> findOlderFileMessages(
            String conversationId,
            Message.MessageType type,
            LocalDateTime time,
            String id,
            Pageable pageable
    );
}
