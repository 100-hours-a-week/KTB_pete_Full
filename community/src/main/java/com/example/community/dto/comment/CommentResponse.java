package com.example.community.dto.comment;

public class CommentResponse {
    public Long id;
    public Long postId;
    public Long userId;
    public String content;
    public String createdAt;
    public String updatedAt;

    public CommentResponse(Long id, Long postId, Long userId, String content,
                           String createdAt, String updatedAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
