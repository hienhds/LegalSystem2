package com.example.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteEvent {
    private Long inviteId;
    private String conversationName;
    private Long senderId;
    private Long receiverId;
    private String type;
    private String note;
    private LocalDateTime timestamp;
}