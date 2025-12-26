package com.example.backend.rating.entity;

import com.example.backend.forum.entity.ForumPost;
import com.example.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_rating", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForumRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long forumRatingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private ForumPost post;

    // luật sư được đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    private User lawyer;

    // người đánh giá
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;  //1–5

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
