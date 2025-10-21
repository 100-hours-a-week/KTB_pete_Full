package com.example.community.domain; // 도메인 패키지

import java.time.Instant; // 시간 표현을 위해 Instant 사용

/**
 * 댓글 도메인(엔티티).
 * - 게시글(postId)에 종속되는 리소스.
 * - 컨트롤러 입출력은 DTO를 사용하고, 내부 로직/저장은 이 모델을 사용한다.
 */
public class Comment {
    private Long id;           // 댓글 고유 ID
    private Long postId;       // 소속 게시글 ID
    private Long authorId;     // 작성자 사용자 ID
    private String content;    // 내용
    private Instant createdAt; // 생성 시각
    private Instant updatedAt; // 수정 시각

    // 기본 생성자(프레임워크/역직렬화용)
    public Comment() {}

    // 전체 필드를 초기화하는 생성자
    public Comment(Long id, Long postId, Long authorId, String content, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 게터/세터
    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setPostId(Long postId) { this.postId = postId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
