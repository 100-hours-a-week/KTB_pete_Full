package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.common.sort.CommentSortBy;
import com.example.community.common.sort.SortDir;
import com.example.community.domain.Comment;
import com.example.community.domain.Post;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository comments;
    private final PostRepository posts;
    private final UserRepository users;

    public CommentService(CommentRepository comments, PostRepository posts, UserRepository users) {
        this.comments = comments;
        this.posts = posts;
        this.users = users;
    }

    private Comment getByIdAndPostIdOrThrow(Long postId, Long commentId) {
        return comments.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));
    }

    @Transactional
    public Comment create(Long postId, Long authorId, String content) {
        Post post = posts.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));

        users.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (content == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Comment c = Comment.create(post.getId(), authorId, trimmed);
        Comment saved = comments.save(c);

        post.increaseComments();
        post.touchUpdatedAt(Instant.now());
        posts.save(post);

        return saved;
    }

    public Comment getOne(Long commentId) {
        return comments.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));
    }

    @Transactional
    public Comment update(Long postId, Long commentId, Long requesterId, String newContent) {
        Comment c = getByIdAndPostIdOrThrow(postId, commentId);

        Long authorId = c.getAuthorId();
        if (authorId == null || !authorId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        c.updateContent(newContent);
        c.touchUpdatedAt(Instant.now());
        return comments.save(c);
    }

    @Transactional
    public void delete(Long postId, Long commentId, Long requesterId) {
        Comment c = getByIdAndPostIdOrThrow(postId, commentId);

        Long authorId = c.getAuthorId();
        if (authorId == null || !authorId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comments.deleteById(commentId);

        posts.findById(c.getPostId()).ifPresent(p -> {
            p.decreaseCommentsIfPossible();
            p.touchUpdatedAt(Instant.now());
            posts.save(p);
        });
    }

    // DB에서 바로 정렬/페이징 메서드
    public org.springframework.data.domain.Page<Comment> listByPostPaged(
            Long postId,
            int page,
            int size,
            CommentSortBy sortBy,
            SortDir direction
    ) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
        
        String sortProperty = "createdAt";

        Sort.Direction dir = Sort.Direction.DESC;
        if (direction == SortDir.ASC) {
            dir = Sort.Direction.ASC;
        }

        Sort sort = Sort.by(dir, sortProperty);
        PageRequest pageable = PageRequest.of(page, size, sort);

        return comments.findByPostId(postId, pageable);
    }

    public long countByPost(Long postId) {
        return comments.countByPostId(postId);
    }
}
