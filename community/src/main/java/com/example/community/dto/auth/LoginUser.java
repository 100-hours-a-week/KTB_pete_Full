package com.example.community.dto.auth;

public class LoginUser {
    public String id;
    public String email;
    public String nickname;
    public String profileImage;

    public LoginUser(String id, String email, String nickname, String profileImage) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }
}