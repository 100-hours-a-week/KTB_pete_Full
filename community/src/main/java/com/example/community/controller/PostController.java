// controller/PostController.java
package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.web.CurrentUserId;
import com.example.community.common.security.TokenUtil;
import com.example.community.common.sort.PostSortBy;
import com.example.community.common.sort.SortDir;
import com.example.community.dto.post.PageMeta;
import com.example.community.dto.post.PostCreateRequest;
import com.example.community.dto.post.PostListResponse;
import com.example.community.dto.post.PostResponse;
import com.example.community.dto.post.PostUpdateRequest;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.mapper.PostMapper;
import com.example.community.service.PostLikeService;
import com.example.community.service.PostService;
import com.example.community.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/board/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final PostLikeService postLikeService;

    public PostController(PostService postService, UserService userService, PostLikeService postLikeService) {
        this.postService = postService;
        this.userService = userService;
        this.postLikeService = postLikeService;
    }

    // 게시글 생성
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "게시글 생성")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "검증 실패 (예시)",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"isSuccess\":false,\"code\":400,\"message\":\"요청 값이 올바르지 않습니다.\",\"result\":null}"
                            )
                    )
            )
    })
    public ApiResponse<PostResponse> create(
            @CurrentUserId Long userId,
            @Valid @RequestBody PostCreateRequest body
    ) {
        Post p = postService.create(userId, body.title, body.content, body.image);
        User author = userService.getMe(userId);
        PostResponse res = PostMapper.toResponse(p, author, false);
        return ApiResponse.ok("게시글 작성 성공", res);
    }

    // 게시글 단건 조회 (작성자 정보 + 현재 유저 liked 포함)
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getOne(
            @PathVariable("postId") Long postId,
            HttpServletRequest request
    ) {
        Post p = postService.getOne(postId); // views 증가 포함

        User author = null;
        if (p != null && p.getAuthorId() != null) {
            author = userService.getMe(p.getAuthorId());
        }

        boolean liked = false;
        if (request != null) {
            String auth = request.getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                Long currentUserId = TokenUtil.resolveUserId(auth);
                liked = postLikeService.isLiked(postId, currentUserId);
            }
        }

        PostResponse res = PostMapper.toResponse(p, author, liked);
        return ApiResponse.ok("게시글 불러오기 성공", res);
    }

    // 게시글 목록 조회 (작성자 정보 포함, liked는 항상 false)
    @GetMapping
    public ApiResponse<PostListResponse> list(
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

        PostSortBy sortBy = PostSortBy.from(sort);
        SortDir direction = SortDir.from(dir);

        org.springframework.data.domain.Page<Post> pageResult =
                postService.listAllPaged(p, s, sortBy, direction);

        List<Post> data = pageResult.getContent();
        long totalElements = pageResult.getTotalElements();
        int totalPages = pageResult.getTotalPages();

        // 1) 작성자 id 수집
        Set<Long> authorIds = new HashSet<>();
        int sizeList = data.size();
        int idx = 0;
        while (idx < sizeList) {
            Post post = data.get(idx);
            if (post != null) {
                Long aid = post.getAuthorId();
                if (aid != null) {
                    authorIds.add(aid);
                }
            }
            idx = idx + 1;
        }

        // 2) 작성자 일괄 조회
        Map<Long, User> authorMap = userService.findByIds(authorIds);

        // 3) 응답 변환 (liked는 목록에선 false)
        List<PostResponse> items = PostMapper.toResponseList(data, authorMap);

        PageMeta meta = new PageMeta(p, s, totalElements, totalPages);
        PostListResponse payload = new PostListResponse(items, meta);
        return ApiResponse.ok("게시글 목록 불러오기 성공", payload);
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "게시글 수정", description = "작성자 불일치 시 403 반환 가능")
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
    public ApiResponse<PostResponse> update(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @RequestBody PostUpdateRequest body
    ) {
        Post p = postService.update(postId, userId, body.title, body.content, body.image);

        User author = null;
        if (p != null && p.getAuthorId() != null) {
            author = userService.getMe(p.getAuthorId());
        }

        PostResponse res = PostMapper.toResponse(p, author, false);
        return ApiResponse.ok("게시글 수정하기 성공", res);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId
    ) {
        postService.delete(postId, userId);
        return ApiResponse.ok("게시글 삭제 성공", null);
    }
}
