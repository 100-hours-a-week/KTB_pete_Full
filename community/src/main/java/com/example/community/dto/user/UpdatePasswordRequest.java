package com.example.community.dto.user;

import jakarta.validation.constraints.NotBlank;

public class UpdatePasswordRequest {
//    @NotBlank
//    public String oldPassword;

    @NotBlank
    public String newPassword;

    @NotBlank
    public String confirmPassword;
}
