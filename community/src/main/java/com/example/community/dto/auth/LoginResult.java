package com.example.community.dto.auth;

public class LoginResult {
    public String token;
    public LoginUser user;

    public LoginResult(String token, LoginUser user) {
        this.token = token;
        this.user = user;
    }
}