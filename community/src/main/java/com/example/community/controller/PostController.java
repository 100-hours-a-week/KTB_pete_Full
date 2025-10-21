package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.TokenUtil;
import com.example.community.dto.post.PageMeta;
import com.example.community.dto.post.PostCreateRequest;
import com.example.community.dto.post.PostListResponse;
import com.example.community.dto.post.PostResponse;
import com.example.community.dto.post.PostUpdateRequest;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.service.PostService;
import com.example.community.service.UserService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/board/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    // 게시글 생성
    @PostMapping
    public ApiResponse<PostResponse> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody PostCreateRequest body
    ) {
        Long authorId = TokenUtil.resolveUserId(auth);

        Post p = postService.create(authorId, body.title, body.content, body.image);
        User author = userService.getMe(authorId);

        String createdAtStr = (p.getCreatedAt() != null) ? p.getCreatedAt().toString() : "unknown";
        String updatedAtStr = (p.getUpdatedAt() != null) ? p.getUpdatedAt().toString() : "unknown";

        PostResponse res = new PostResponse(
                p.getId(), author.getId(), p.getTitle(), p.getContent(),
                p.getImage(),
                String.valueOf(p.getComments() == null ? 0 : p.getComments()),
                String.valueOf(p.getLikes() == null ? 0 : p.getLikes()),
                String.valueOf(p.getViews() == null ? 0 : p.getViews()),
                createdAtStr, updatedAtStr
        );

        return ApiResponse.ok("게시글 작성 성공", res);
    }

    // 게시글 단건 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getOne(@PathVariable("postId") Long postId) {
        Post p = postService.getOne(postId); // views 증가가 필요하면 서비스에서 처리
        User author = userService.getMe(p.getAuthorId());

        String createdAtStr = (p.getCreatedAt() != null) ? p.getCreatedAt().toString() : "unknown";
        String updatedAtStr = (p.getUpdatedAt() != null) ? p.getUpdatedAt().toString() : "unknown";

        PostResponse res = new PostResponse(
                p.getId(), author.getId(), p.getTitle(), p.getContent(),
                p.getImage(),
                String.valueOf(p.getComments() == null ? 0 : p.getComments()),
                String.valueOf(p.getLikes() == null ? 0 : p.getLikes()),
                String.valueOf(p.getViews() == null ? 0 : p.getViews()),
                createdAtStr, updatedAtStr
        );

        return ApiResponse.ok("게시글 불러오기 성공", res);
    }

    // 게시글 목록
    @GetMapping
    public ApiResponse<PostListResponse> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "dir", required = false) String dir
    ) {
        int p = (page != null && page.intValue() >= 0) ? page.intValue() : 0;
        int s = (size != null && size.intValue() > 0) ? size.intValue() : 10;
        String sortKey = (sort != null) ? sort : "createdAt";
        String direction = (dir != null) ? dir : "desc";

        List<Post> data = postService.listAllSortedPaged(p, s, sortKey, direction);
        long totalElements = postService.countAll();
        int totalPages = (int)((totalElements + s - 1) / s);

        List<PostResponse> items = new ArrayList<>();
        int i = 0;
        int sizeList = data.size();
        while (i < sizeList) {
            Post post = data.get(i);
            User author = userService.getMe(post.getAuthorId());

            String createdAtStr = (post.getCreatedAt() != null) ? post.getCreatedAt().toString() : "unknown";
            String updatedAtStr = (post.getUpdatedAt() != null) ? post.getUpdatedAt().toString() : "unknown";

            items.add(new PostResponse(
                    post.getId(), author.getId(), post.getTitle(), post.getContent(),
                    post.getImage(),
                    String.valueOf(post.getComments() == null ? 0 : post.getComments()),
                    String.valueOf(post.getLikes() == null ? 0 : post.getLikes()),
                    String.valueOf(post.getViews() == null ? 0 : post.getViews()),
                    createdAtStr, updatedAtStr
            ));

            i = i + 1;
        }

        PageMeta meta = new PageMeta(p, s, totalElements, totalPages);
        PostListResponse payload = new PostListResponse(items, meta);
        return ApiResponse.ok("게시글 목록 불러오기 성공", payload);
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ApiResponse<PostResponse> update(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable("postId") Long postId,
            @RequestBody PostUpdateRequest body
    ) {
        Long requesterId = TokenUtil.resolveUserId(auth);
        Post p = postService.update(postId, requesterId, body.title, body.content, body.image);

        User author = userService.getMe(p.getAuthorId());
        String createdAtStr = (p.getCreatedAt() != null) ? p.getCreatedAt().toString() : "unknown";
        String updatedAtStr = (p.getUpdatedAt() != null) ? p.getUpdatedAt().toString() : "unknown";

        PostResponse res = new PostResponse(
                p.getId(), author.getId(), p.getTitle(), p.getContent(),
                p.getImage(),
                String.valueOf(p.getComments() == null ? 0 : p.getComments()),
                String.valueOf(p.getLikes() == null ? 0 : p.getLikes()),
                String.valueOf(p.getViews() == null ? 0 : p.getViews()),
                createdAtStr, updatedAtStr
        );

        return ApiResponse.ok("게시글 수정하기 성공", res);
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable("postId") Long postId
    ) {
        Long requesterId = TokenUtil.resolveUserId(auth);
        postService.delete(postId, requesterId);
        return ApiResponse.ok("게시글 삭제 성공", null);
    }
}
