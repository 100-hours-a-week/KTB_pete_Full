// dto/post/PostResponse.java
package com.example.community.dto.post;

public class PostResponse {
    public Long id;
    public Long userId;
    public String writerNickname;
    public String writerProfileImage;
    public String title;
    public String content;
    public String image;
    public String comments;
    public String likes;
    public String views;
    public String liked;
    public String createdAt;
    public String updatedAt;

    public PostResponse(
            Long id,
            Long userId,
            String writerNickname,
            String writerProfileImage,
            String title,
            String content,
            String image,
            String comments,
            String likes,
            String views,
            String liked,
            String createdAt,
            String updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.writerNickname = writerNickname;
        this.writerProfileImage = writerProfileImage;
        this.title = title;
        this.content = content;
        this.image = image;
        this.comments = comments;
        this.likes = likes;
        this.views = views;
        this.liked = liked;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
