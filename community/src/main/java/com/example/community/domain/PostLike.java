package com.example.community.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "post_likes")
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostLike() {
    }

    private PostLike(Long id, Long postId, Long userId, Instant createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public static PostLike create(Long postId, Long userId) {
        Instant now = Instant.now();
        return new PostLike(null, postId, userId, now);
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
}
