package com.example.chatservice.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DissolveGroupEvent {
    private List<Long> memberIds;
    private String conversationName;
    private Long ownerId;
    private LocalDateTime timestamp;
}
