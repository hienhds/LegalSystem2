package com.example.chatservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversations",
        indexes = {
                @Index(name = "idx_conv_created_by", columnList = "created_by"),
                @Index(name = "idx_conv_last_message_at", columnList = "last_message_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id; // UUID string

    @Column( length = 255)
    private String name;  // neeus la private tu clent tu render theo ten cua doi phuong

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Conversation.ConversationType type; // PRIVATE, GROUP

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl; // // neeus la private tu clent tu render theo ten cua doi phuong

    @Column(name = "created_by")
    private Long createdByUserId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_avatar")
    private String ownerAvatar;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isLocked = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = false;

    @Column(name = "last_message_text", length = 1024)
    private String lastMessageText;

    @Column(name = "last_message_sender_name", length = 36)
    private String lastMessageSenderName;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "note", length = 512)
    private String note;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<ConversationMember> members = new HashSet<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ConversationInvite> invites = new HashSet<>();

    public enum ConversationType {
        DIRECT,  // Chat 1-1

        PUBLIC,
        GROUP     // Chat nhóm
    }
}