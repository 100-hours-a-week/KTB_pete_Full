package com.example.community.domain;

import java.time.Instant;

/**
 * 게시글 도메인(엔티티).
 * - PDF 명세 일치 버전
 * - image / comments / likes / views 필드 추가
 */
public class Post {
    private Long id;           // 게시글 고유 ID
    private Long authorId;     // 작성자 ID (User.id 참조)
    private String title;      // 제목
    private String content;    // 내용
    private String image;      // 대표 이미지 URL (nullable)
    private Long comments;     // 댓글 수
    private Long likes;        // 좋아요 수
    private Long views;        // 조회수
    private Instant createdAt; // 생성 시각
    private Instant updatedAt; // 수정 시각

    public Post() {}

    // 전체 필드 초기화용 생성자
    public Post(Long id, Long authorId, String title, String content,
                String image, Long comments, Long likes, Long views,
                Instant createdAt, Instant updatedAt) {
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

    // 세터
    public void setId(Long id) { this.id = id; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setImage(String image) { this.image = image; }
    public void setComments(Long comments) { this.comments = comments; }
    public void setLikes(Long likes) { this.likes = likes; }
    public void setViews(Long views) { this.views = views; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
