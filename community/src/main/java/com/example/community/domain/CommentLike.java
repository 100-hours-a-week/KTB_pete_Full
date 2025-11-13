package com.example.community.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "comment_likes")
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CommentLike() {
    }

    private CommentLike(Long id, Long commentId, Long userId, Instant createdAt) {
        this.id = id;
        this.commentId = commentId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static CommentLike create(Long commentId, Long userId) {
        Instant now = Instant.now();
        return new CommentLike(null, commentId, userId, now);
    }

    public Long getId() { return id; }
    public Long getCommentId() { return commentId; }
    public Long getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
}
