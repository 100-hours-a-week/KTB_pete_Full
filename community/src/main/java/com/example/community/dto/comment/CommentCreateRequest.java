package com.example.community.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class CommentCreateRequest {
    @NotBlank
    public String content;

    @JsonProperty("post_id")
    public String post_id; // null 허용
}
