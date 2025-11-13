package com.example.community.dto.comment;

public class CommentResponse {
    public Long id;
    public Long postId;
    public Long userId;
    public String content;
    public String likes;
    public String createdAt;
    public String updatedAt;

    public CommentResponse(Long id, Long postId, Long userId, String content,
                           String likes,String createdAt, String updatedAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.likes = likes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
