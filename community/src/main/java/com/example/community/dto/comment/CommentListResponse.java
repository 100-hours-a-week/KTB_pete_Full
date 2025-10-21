package com.example.community.dto.comment;

import java.util.List;

public class CommentListResponse {
    public java.util.List<CommentResponse> items; // 댓글 아이템 배열
    public com.example.community.dto.post.PageMeta page; // 페이지 정보

    public CommentListResponse(List<CommentResponse> items, com.example.community.dto.post.PageMeta page) {
        this.items = items;
        this.page = page;
    }
}
