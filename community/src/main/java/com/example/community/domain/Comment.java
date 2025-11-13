package com.example.community.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Comment() {
        // JPA 기본 생성자
    }

    private Comment(Long id, Long postId, Long authorId, String content,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Comment create(Long postId, Long authorId, String content) {
        Instant now = Instant.now();
        return new Comment(null, postId, authorId, content, now, now);
    }

    public void updateContent(String newContent) {
        if (newContent != null) {
            String t = newContent.trim();
            if (!t.isEmpty()) {
                this.content = t;
            }
        }
    }

    public void touchUpdatedAt(Instant now) {
        this.updatedAt = now;
    }

    // 게터
    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
