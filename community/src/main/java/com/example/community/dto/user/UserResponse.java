package com.example.community.dto.user;

public class UserResponse {
    public Long id;
    public String email;
    public String nickname;
    public String profileImage; // 네이밍 변경
    public String createdAt;
    public String updatedAt;    // 추가(명세 예시 반영)

    public UserResponse(Long id, String email, String nickname, String profileImage,
                        String createdAt, String updatedAt) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
