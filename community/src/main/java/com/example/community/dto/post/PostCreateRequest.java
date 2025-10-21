package com.example.community.dto.post;

import jakarta.validation.constraints.NotBlank;

public class PostCreateRequest {
    @NotBlank
    public String title;

    @NotBlank
    public String content;

    public String image;
}
