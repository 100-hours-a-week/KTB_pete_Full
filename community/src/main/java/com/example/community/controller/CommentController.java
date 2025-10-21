package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.common.TokenUtil;
import com.example.community.dto.comment.CommentCreateRequest;
import com.example.community.dto.comment.CommentListResponse;
import com.example.community.dto.comment.CommentResponse;
import com.example.community.dto.comment.CommentUpdateRequest;
import com.example.community.dto.post.PageMeta;
import com.example.community.domain.Comment;
import com.example.community.domain.User;
import com.example.community.service.CommentService;
import com.example.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/board/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    // 생성
    @PostMapping
    public ApiResponse<CommentResponse> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CommentCreateRequest body
    ) {
        // body.post_id가 왔다면 PathVariable과 반드시 일치해야 함 → 불일치면 400
        if (body.post_id != null && !body.post_id.equals(String.valueOf(postId))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Long authorId = TokenUtil.resolveUserId(auth);
        Comment c = commentService.create(postId, authorId, body.content);

        User author = userService.getMe(authorId);

        String createdAtStr = (c.getCreatedAt() != null) ? c.getCreatedAt().toString() : "unknown";
        String updatedAtStr = (c.getUpdatedAt() != null) ? c.getUpdatedAt().toString() : "unknown";

        CommentResponse res = new CommentResponse(
                c.getId(),
                c.getPostId(),
                author.getId(),
                c.getContent(),
                createdAtStr,
                updatedAtStr
        );
        return ApiResponse.ok("댓글 작성 성공", res);
    }

    // 목록
    @GetMapping
    public ApiResponse<CommentListResponse> list(
            @PathVariable("postId") Long postId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "dir", required = false) String dir
    ) {
        int p = (page != null && page.intValue() >= 0) ? page.intValue() : 0;
        int s = (size != null && size.intValue() > 0) ? size.intValue() : 10;
        String sortKey = (sort != null) ? sort : "createdAt";
        String direction = (dir != null) ? dir : "desc";

        List<Comment> data = commentService.listByPostPaged(postId, p, s, sortKey, direction);

        long totalElements = commentService.countByPost(postId);
        int totalPages = (int) ((totalElements + s - 1) / s);

        List<CommentResponse> items = new ArrayList<>();
        int i = 0;
        int sizeList = data.size();
        while (i < sizeList) {
            Comment c = data.get(i);
            User author = userService.getMe(c.getAuthorId());

            String createdAtStr = (c.getCreatedAt() != null) ? c.getCreatedAt().toString() : "unknown";
            String updatedAtStr = (c.getUpdatedAt() != null) ? c.getUpdatedAt().toString() : "unknown";

            items.add(new CommentResponse(
                    c.getId(),
                    c.getPostId(),
                    author.getId(),
                    c.getContent(),
                    createdAtStr,
                    updatedAtStr
            ));
            i = i + 1;
        }

        PageMeta meta = new PageMeta(p, s, totalElements, totalPages);
        CommentListResponse payload = new CommentListResponse(items, meta);
        return ApiResponse.ok("댓글 목록 불러오기 성공", payload);
    }

    // 수정
    @PatchMapping("/{commentId}")
    public ApiResponse<CommentResponse> update(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentUpdateRequest body
    ) {
        Long requesterId = TokenUtil.resolveUserId(auth);
        Comment c = commentService.update(commentId, requesterId, body.content);

        User author = userService.getMe(c.getAuthorId());
        String createdAtStr = (c.getCreatedAt() != null) ? c.getCreatedAt().toString() : "unknown";
        String updatedAtStr = (c.getUpdatedAt() != null) ? c.getUpdatedAt().toString() : "unknown";

        CommentResponse res = new CommentResponse(
                c.getId(),
                c.getPostId(),
                author.getId(),
                c.getContent(),
                createdAtStr,
                updatedAtStr
        );
        return ApiResponse.ok("댓글 수정 성공", res);
    }

    // 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId
    ) {
        Long requesterId = TokenUtil.resolveUserId(auth);
        commentService.delete(commentId, requesterId);
        return ApiResponse.ok("댓글 삭제 성공", null);
    }
}
