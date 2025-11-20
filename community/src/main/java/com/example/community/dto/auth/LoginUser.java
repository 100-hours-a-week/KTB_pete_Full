package com.example.community.dto.auth;

public class LoginUser {
    public String id;
    public String email;

    public LoginUser(String id, String email) {
        this.id = id;
        this.email = email;
    }
}