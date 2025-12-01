package com.example.community.dto.comment;

public class CommentResponse {
    public Long id;
    public Long postId;
    public Long userId;
    public String writerNickname;
    public String writerProfileImage;
    public String content;
    public String likes;
    public String createdAt;
    public String updatedAt;

    public CommentResponse(
            Long id,
            Long postId,
            Long userId,
            String writerNickname,
            String writerProfileImage,
            String content,
            String likes,
            String createdAt,
            String updatedAt
    ) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.writerNickname = writerNickname;
        this.writerProfileImage = writerProfileImage;
        this.content = content;
        this.likes = likes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
