package com.example.community.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SignupRequest {
    @NotBlank
    public String nickname;

    @Email
    @NotBlank
    public String email;

    @NotBlank
    public String password;

    public String profileImage;
}
