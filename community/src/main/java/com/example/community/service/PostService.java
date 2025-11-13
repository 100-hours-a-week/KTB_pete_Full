package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.common.sort.PostSortBy;
import com.example.community.common.sort.SortDir;
import com.example.community.common.util.Numbers;
import com.example.community.domain.Post;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository posts;
    private final UserRepository users;

    public PostService(PostRepository posts, UserRepository users) {
        this.posts = posts;
        this.users = users;
    }

    @Transactional
    public Post create(Long authorId, String title, String content, String image) {
        users.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Post p = Post.create(authorId, title, content, image);
        return posts.save(p);
    }

    @Transactional
    public Post getOne(Long postId) {
        Post p = posts.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));

        p.increaseViews();
        p.touchUpdatedAt(Instant.now());
        return posts.save(p);
    }

    @Transactional
    public Post update(Long postId, Long requesterId, String newTitle, String newContent, String newImage) {
        Post p = posts.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));

        Long authorId = p.getAuthorId();
        if (authorId == null || !authorId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        p.updatePartial(newTitle, newContent, newImage);
        p.touchUpdatedAt(Instant.now());
        return posts.save(p);
    }

    @Transactional
    public void delete(Long postId, Long requesterId) {
        Post p = posts.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));

        Long authorId = p.getAuthorId();
        if (authorId == null || !authorId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        posts.deleteById(postId);
    }

    public Page<Post> listAllPaged(int page, int size, PostSortBy sortBy, SortDir direction) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        // 정렬 기준 필드명 매핑
        String sortProperty = "createdAt"; // 기본값
        if (sortBy == PostSortBy.TITLE) {
            sortProperty = "title";
        } else if (sortBy == PostSortBy.VIEWS) {
            sortProperty = "views";
        } else if (sortBy == PostSortBy.LIKES) {
            sortProperty = "likes";
        }

        Sort.Direction dir = Sort.Direction.DESC;
        if (direction == SortDir.ASC) {
            dir = Sort.Direction.ASC;
        }

        Sort sort = Sort.by(dir, sortProperty);
        PageRequest pageable = PageRequest.of(page, size, sort);

        return posts.findAll(pageable);
    }

    public long countAll() {
        return posts.count();
    }
}
