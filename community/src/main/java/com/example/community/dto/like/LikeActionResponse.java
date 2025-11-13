package com.example.community.dto.like;

public class LikeActionResponse {
    public boolean liked;      // 현재 좋아요 상태
    public String likeCount;   // 문자열(프런트 표준화)

    public LikeActionResponse(boolean liked, String likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }
}
