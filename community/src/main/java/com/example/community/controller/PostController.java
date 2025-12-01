package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.security.JwtProvider;
import com.example.community.common.sort.PostSortBy;
import com.example.community.common.sort.SortDir;
import com.example.community.common.web.CurrentUserId;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.dto.post.PageMeta;
import com.example.community.dto.post.PostListResponse;
import com.example.community.dto.post.PostResponse;
import com.example.community.mapper.PostMapper;
import com.example.community.service.PostLikeService;
import com.example.community.service.PostService;
import com.example.community.service.UserService;
import com.example.community.storage.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@RestController
@RequestMapping("/board/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final PostLikeService postLikeService;
    private final FileStorageService fileStorageService;
    private final JwtProvider jwtProvider;

    public PostController(PostService postService,
                          UserService userService,
                          PostLikeService postLikeService,
                          FileStorageService fileStorageService,
                          JwtProvider jwtProvider) {
        this.postService = postService;
        this.userService = userService;
        this.postLikeService = postLikeService;
        this.fileStorageService = fileStorageService;
        this.jwtProvider = jwtProvider;

    }

    // 게시글 생성 (multipart/form-data, 파일 업로드)
    @PostMapping(
            value = "",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @io.swagger.v3.oas.annotations.Operation(summary = "게시글 생성 (multipart/form-data)")
    public ApiResponse<PostResponse> create(
            @CurrentUserId Long userId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.store(image, "posts");
        }

        Post p = postService.create(userId, title, content, imageUrl);

        User author = userService.getMe(userId);
        PostResponse res = PostMapper.toResponse(p, author, false);

        return ApiResponse.ok("게시글 작성 성공", res);
    }

    // 게시글 단건 조회 (작성자 정보, 좋아요 여부 포함)
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getOne(
            @PathVariable("postId") Long postId
    ) {
        // 1) 게시글, 작성자 조회
        Post p = postService.getOne(postId);

        Long authorId = (p != null ? p.getAuthorId() : null);
        User author = null;
        if (authorId != null) {
            author = userService.getMe(authorId);
        }

        // 2) 기본값: 익명 사용자 → liked = false
        boolean liked = false;

        // 3) SecurityContext에 인증 정보가 있으면 userId 꺼내서 좋아요 여부 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            Long currentUserId = (Long) auth.getPrincipal();
            liked = postLikeService.isLiked(postId, currentUserId);
        }

        PostResponse res = PostMapper.toResponse(p, author, liked);
        return ApiResponse.ok("게시글 불러오기 성공", res);
    }


    // 게시글 목록 조회 (작성자 정보만 포함, liked는 포함 X)
    @GetMapping
    public ApiResponse<PostListResponse> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "dir", required = false) String dir
    ) {
        int p = 0;
        if (page != null && page >= 0) {
            p = page;
        }
        int s = 10;
        if (size != null && size > 0) {
            s = size;
        }

        PostSortBy sortBy = PostSortBy.from(sort);
        SortDir direction = SortDir.from(dir);

        org.springframework.data.domain.Page<Post> pageResult =
                postService.listAllPaged(p, s, sortBy, direction);

        java.util.List<Post> data = pageResult.getContent();
        long totalElements = pageResult.getTotalElements();
        int totalPages = pageResult.getTotalPages();

        java.util.Set<Long> authorIds = new java.util.HashSet<>();
        for (Post post : data) {
            if (post != null && post.getAuthorId() != null) {
                authorIds.add(post.getAuthorId());
            }
        }

        java.util.Map<Long, User> authorMap = userService.findByIds(authorIds);
        java.util.List<PostResponse> items = PostMapper.toResponseList(data, authorMap);

        PageMeta meta = new PageMeta(p, s, totalElements, totalPages);
        PostListResponse payload = new PostListResponse(items, meta);
        return ApiResponse.ok("게시글 목록 불러오기 성공", payload);
    }

    // 게시글 수정
    @PatchMapping(
            value = "/{postId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @io.swagger.v3.oas.annotations.Operation(
            summary = "게시글 수정",
            description = "작성자 불일치 시 403 반환 가능"
    )
    public ApiResponse<PostResponse> update(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @RequestParam("title") String title,      // Text
            @RequestParam("content") String content,  // Text
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = fileStorageService.store(imageFile, "posts");
        }

        Post p = postService.update(postId, userId, title, content, imageUrl);

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
