package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.util.Numbers;
import com.example.community.common.web.CurrentUserId;
import com.example.community.dto.like.LikeActionResponse;
import com.example.community.service.CommentLikeService;
import com.example.community.service.PostLikeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/board")
public class LikeController {

    private final PostLikeService postLikes;
    private final CommentLikeService commentLikes;

    public LikeController(PostLikeService postLikes, CommentLikeService commentLikes) {
        this.postLikes = postLikes;
        this.commentLikes = commentLikes;
    }

    // 게시글 좋아요 추가
    @PostMapping("/posts/{postId}/likes")
    @io.swagger.v3.oas.annotations.Operation(summary = "게시글 좋아요")
    public ApiResponse<LikeActionResponse> likePost(
            @PathVariable("postId") Long postId,
            @CurrentUserId Long userId
    ) {
        boolean created = postLikes.like(postId, userId);
        long cnt = postLikes.count(postId);
        LikeActionResponse res = new LikeActionResponse(true, Numbers.toStringOrZero(cnt));
        return ApiResponse.ok("게시글 좋아요 성공", res);
    }

    // 게시글 좋아요 취소
    @DeleteMapping("/posts/{postId}/likes")
    @io.swagger.v3.oas.annotations.Operation(summary = "게시글 좋아요 취소")
    public ApiResponse<LikeActionResponse> unlikePost(
            @PathVariable("postId") Long postId,
            @CurrentUserId Long userId
    ) {
        boolean removed = postLikes.unlike(postId, userId);
        long cnt = postLikes.count(postId);
        LikeActionResponse res = new LikeActionResponse(false, Numbers.toStringOrZero(cnt));
        return ApiResponse.ok("게시글 좋아요 취소 성공", res);
    }

    // 댓글 좋아요 추가
    @PostMapping("/posts/{postId}/comments/{commentId}/likes")
    @io.swagger.v3.oas.annotations.Operation(summary = "댓글 좋아요")
    public ApiResponse<LikeActionResponse> likeComment(
            @PathVariable("commentId") Long commentId,
            @CurrentUserId Long userId
    ) {
        boolean created = commentLikes.like(commentId, userId);
        long cnt = commentLikes.count(commentId);
        LikeActionResponse res = new LikeActionResponse(true, Numbers.toStringOrZero(cnt));
        return ApiResponse.ok("댓글 좋아요 성공", res);
    }

    // 댓글 좋아요 취소
    @DeleteMapping("/posts/{postId}/comments/{commentId}/likes")
    @io.swagger.v3.oas.annotations.Operation(summary = "댓글 좋아요 취소")
    public ApiResponse<LikeActionResponse> unlikeComment(
            @PathVariable("commentId") Long commentId,
            @CurrentUserId Long userId
    ) {
        boolean removed = commentLikes.unlike(commentId, userId);
        long cnt = commentLikes.count(commentId);
        LikeActionResponse res = new LikeActionResponse(false, Numbers.toStringOrZero(cnt));
        return ApiResponse.ok("댓글 좋아요 취소 성공", res);
    }
}
