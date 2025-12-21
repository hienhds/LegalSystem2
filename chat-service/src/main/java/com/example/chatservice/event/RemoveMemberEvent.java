package com.example.chatservice.event;
import lombok.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemoveMemberEvent {
    private String conversationName;
    private Long memberId;
    private LocalDateTime timestamp;
}
