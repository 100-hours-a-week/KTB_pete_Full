package com.example.community.domain;

import com.example.community.common.util.Numbers;
import com.example.community.common.util.Strings;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여기서는 관계를 안 쓰고 authorId만 저장 (기존 코드와 호환)
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String image;

    @Column
    private Long comments;

    @Column
    private Long likes;

    @Column
    private Long views;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Post() {
    }

    private Post(Long id, Long authorId, String title, String content, String image,
                 Long comments, Long likes, Long views, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.image = image;
        this.comments = comments;
        this.likes = likes;
        this.views = views;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Post create(Long authorId, String title, String content, String image) {
        Instant now = Instant.now();
        return new Post(
                null,
                authorId,
                title,
                content,
                Strings.nullToEmpty(image),
                0L,
                0L,
                0L,
                now,
                now
        );
    }

    public void touchUpdatedAt(Instant now) {
        this.updatedAt = now;
    }

    public void increaseViews() {
        long v = Numbers.longOrZero(this.views);
        this.views = v + 1L;
    }

    public void increaseComments() {
        long c = Numbers.longOrZero(this.comments);
        this.comments = c + 1L;
    }

    public void decreaseCommentsIfPossible() {
        long c = Numbers.longOrZero(this.comments);
        if (c > 0L) {
            this.comments = c - 1L;
        }
    }

    public void increaseLikes() {
        long v = Numbers.longOrZero(this.likes);
        this.likes = v + 1L;
    }

    public void decreaseLikesIfPossible() {
        long v = Numbers.longOrZero(this.likes);
        if (v > 0L) {
            this.likes = v - 1L;
        }
    }

    public void updatePartial(String newTitle, String newContent, String newImage) {
        if (newTitle != null) {
            String t = newTitle.trim();
            if (!t.isEmpty()) {
                this.title = t;
            }
        }
        if (newContent != null) {
            String c = newContent.trim();
            if (!c.isEmpty()) {
                this.content = c;
            }
        }
        if (newImage != null) {
            String i = newImage.trim();
            if (!i.isEmpty()) {
                this.image = i;
            }
        }
    }

    // 게터
    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImage() { return image; }
    public Long getComments() { return comments; }
    public Long getLikes() { return likes; }
    public Long getViews() { return views; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
