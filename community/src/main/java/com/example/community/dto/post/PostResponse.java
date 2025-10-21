package com.example.community.dto.post;

public class PostResponse {
    public Long id;
    public Long userId;
    public String title;
    public String content;
    public String image;
    public String comments;
    public String likes;
    public String views;
    public String createdAt;
    public String updatedAt;

    public PostResponse(Long id, Long userId, String title, String content,
                        String image, String comments, String likes, String views,
                        String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.image = image;
        this.comments = comments;
        this.likes = likes;
        this.views = views;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
