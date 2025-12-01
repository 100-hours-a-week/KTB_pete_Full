package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.common.web.CurrentUserId;
import com.example.community.common.sort.CommentSortBy;
import com.example.community.common.sort.SortDir;
import com.example.community.dto.comment.*;
import com.example.community.dto.post.PageMeta;
import com.example.community.domain.Comment;
import com.example.community.domain.User;
import com.example.community.mapper.CommentMapper;
import com.example.community.service.CommentLikeService;
import com.example.community.service.CommentService;
import com.example.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/board/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final CommentLikeService commentLikeService;

    public CommentController(CommentService commentService, UserService userService, CommentLikeService commentLikeService) {
        this.commentService = commentService;
        this.userService = userService;
        this.commentLikeService = commentLikeService;
    }

    // 생성
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "댓글 생성", description = "PathVariable과 body.post_id 불일치 시 400")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "요청 값이 올바르지 않습니다.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"isSuccess\":false,\"code\":400,\"message\":\"요청 값이 올바르지 않습니다.\",\"result\":null}"
                            )
                    )
            )
    })
    public ApiResponse<CommentResponse> create(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CommentCreateRequest body
    ) {
        if (body.post_id != null) {
            String expected = String.valueOf(postId);
            if (!expected.equals(body.post_id)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
        }

        Comment c = commentService.create(postId, userId, body.content);
        User author = userService.getMe(userId);
        CommentResponse res = CommentMapper.toResponse(c, author, 0L);
        return ApiResponse.ok("댓글 작성 성공", res);
    }

    @GetMapping
    public ApiResponse<CommentListResponse> list(
            @PathVariable("postId") Long postId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "dir", required = false) String dir
    ) {
        int p = 0;
        if (page != null) {
            int v = page.intValue();
            if (v >= 0) {
                p = v;
            }
        }
        int s = 10;
        if (size != null) {
            int v = size.intValue();
            if (v > 0) {
                s = v;
            }
        }

        CommentSortBy sortBy = CommentSortBy.from(sort); // 현재 createdAt 고정
        SortDir direction = SortDir.from(dir);

        org.springframework.data.domain.Page<Comment> pageResult =
                commentService.listByPostPaged(postId, p, s, sortBy, direction);

        List<Comment> data = pageResult.getContent();
        long totalElements = pageResult.getTotalElements();
        int totalPages = pageResult.getTotalPages();

        // 1) 작성자 id 수집, 댓글 id 수집
        Set<Long> authorIds = new HashSet<Long>();
        List<Long> commentIds = new ArrayList<Long>();

        int sizeList = data.size();
        int i = 0;
        while (i < sizeList) {
            Comment c = data.get(i);
            if (c != null) {
                Long aid = c.getAuthorId();
                if (aid != null) {
                    authorIds.add(aid);
                }

                Long cid = c.getId();
                if (cid != null) {
                    commentIds.add(cid);
                }
            }
            i = i + 1;
        }

        // 2) 일괄 조회 (작성자)
        Map<Long, User> authorMap = userService.findByIds(authorIds);

        // 3) 일괄 조회 (댓글 좋아요 수)
        Map<Long, Long> likeCountMap = commentLikeService.countByCommentIds(commentIds);

        // 4) 매퍼로 변환
        List<CommentResponse> items = CommentMapper.toResponseList(data, authorMap, likeCountMap);

        PageMeta meta = new PageMeta(p, s, totalElements, totalPages);
        CommentListResponse payload = new CommentListResponse(items, meta);
        return ApiResponse.ok("댓글 목록 불러오기 성공", payload);
    }

    // 수정
    @PatchMapping("/{commentId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "댓글 수정", description = "작성자 불일치 시 403 가능")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "권한이 없습니다.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"isSuccess\":false,\"code\":403,\"message\":\"권한이 없습니다.\",\"result\":null}"
                            )
                    )
            )
    })
    public ApiResponse<CommentResponse> update(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentUpdateRequest body
    ) {
        Comment c = commentService.update(postId, commentId, userId, body.content);

        long likeCount = commentLikeService.count(commentId);

        User author = null;
        if (c != null && c.getAuthorId() != null) {
            author = userService.getMe(c.getAuthorId());
        }

        CommentResponse res = CommentMapper.toResponse(c, author, likeCount);
        return ApiResponse.ok("댓글 수정 성공", res);
    }
    // 삭제
    @DeleteMapping("/{commentId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "댓글 삭제", description = "작성자 불일치 시 403 가능")
    public ApiResponse<Void> delete(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId
    ) {
        commentService.delete(postId, commentId, userId);
        return ApiResponse.ok("댓글 삭제 성공", null);
    }
}
