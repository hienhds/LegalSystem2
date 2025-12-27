package com.example.chatservice.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_conv_user", columnNames = {"conversation_id", "user_id"}),
        indexes = {@Index(name = "idx_cm_user", columnList = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMember {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id; // UUID string

    // Many members to one conversation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // user id from user-service
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_avatar")
    private String userAvatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", length = 16)
    private MemberStatus memberStatus;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    // pinned message id (optional)
    @Column(name = "pinned_message_id", length = 36)
    private String pinnedMessageId;

    // Unread counter for this user in this conversation (cached, persisted periodically)
    @Column(name = "unread_count", nullable = false)
    @Builder.Default
    private Integer unreadCount = 0;


    public enum MemberStatus {
        REMOVED, OWNER, MEMBER, OUTED
    }
}
