package com.example.community.dto.auth;

public class LoginUser {
    public String id;
    public String email;
    public String status;

    public LoginUser(String id, String email, String status) {
        this.id = id;
        this.email = email;
        this.status = status;
    }
}