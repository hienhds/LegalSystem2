package com.example.chatservice.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "messages")
@CompoundIndex(
        name = "conv_time_idx",
        def = "{'conversationId': 1, 'timestamp': -1}"
)
@CompoundIndex(
        name = "conv_type_time_idx",
        def = "{'conversationId': 1, 'type': 1, 'timestamp': -1}"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    @Indexed
    private Long senderId;

    private String senderName;
    private String senderAvatar;

    /** TEXT message only */
    private String content;

    /** FILE message only */
    private FileMeta file;

    @Indexed
    private MessageType type;

    @Indexed
    private LocalDateTime timestamp;

    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    public enum MessageType {
        TEXT,
        FILE
    }

    public enum MessageStatus {
        SENT,
        DELIVERED,
        FAILED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileMeta {
        private String fileId;
        private String fileName;
        private String fileUrl;
        private Long fileSize;
        private String mimeType;
    }
}
