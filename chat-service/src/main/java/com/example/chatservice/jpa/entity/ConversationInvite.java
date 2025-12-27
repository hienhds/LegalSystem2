package com.example.chatservice.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_invites", indexes = {@Index(name = "idx_invite_conv", columnList = "conversation_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationInvite {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id; // UUID string

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;


    // user who is invited OR who requested to join (depending on flow)
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_avatar")
    private String receiverAvatar;

    @Column(name = "sender_id")
    private Long senderId; // who invited or who handled the request

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "sender_avatar")
    private String senderAvatar;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    @Builder.Default
    private InviteStatus status = InviteStatus.PENDING;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public enum InviteStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
