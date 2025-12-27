package com.example.chatservice.event;

import com.example.chatservice.mongo.document.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageEvent {
    private String id;
    private String conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Message.MessageType type;
    private String content;
    private Message.FileMeta file;
    private LocalDateTime timestamp;
}
