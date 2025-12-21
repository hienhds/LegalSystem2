package com.example.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
