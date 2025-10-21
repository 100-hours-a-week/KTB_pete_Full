package com.example.community.dto.post;

import java.util.List;

public class PostListResponse {
    public List<PostResponse> items;
    public PageMeta page;

    public PostListResponse(List<PostResponse> items, PageMeta page) {
        this.items = items;
        this.page = page;
    }
}
