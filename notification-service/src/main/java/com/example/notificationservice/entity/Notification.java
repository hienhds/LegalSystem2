package com.example.notificationservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@CompoundIndexes({
        @CompoundIndex(name = "user_read_created_idx",
                def = "{'userId': 1, 'read': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "user_type_created_idx",
                def = "{'userId': 1, 'type': 1, 'createdAt': -1}")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id; // MongoDB ObjectId

    @Indexed
    private Long userId;

    private String title;

    private String content;

    @Builder.Default
    private boolean read = false;

    @Indexed
    private LocalDateTime createdAt;

    private NotificationType type;

    public enum NotificationType {
        INVITE,
        CONVERSATION_CREATED,
        SYSTEM,

        DISSOLVED,

        REMOVED,REJECT
    }
}