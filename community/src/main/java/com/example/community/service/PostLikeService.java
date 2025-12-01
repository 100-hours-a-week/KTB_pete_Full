package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.Post;
import com.example.community.domain.PostLike;
import com.example.community.repository.PostLikeRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository likes;
    private final PostRepository posts;
    private final UserRepository users;

    public PostLikeService(PostLikeRepository likes, PostRepository posts, UserRepository users) {
        this.likes = likes;
        this.posts = posts;
        this.users = users;
    }

    @Transactional
    public boolean like(Long postId, Long userId) {
        Post post = posts.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));
        users.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        boolean exists = likes.existsByUserIdAndPostId(userId, postId);
        if (exists) {
            return false;
        }

        PostLike pl = PostLike.create(postId, userId);
        likes.save(pl);

        post.increaseLikes();
        post.touchUpdatedAt(Instant.now());
        posts.save(post);

        return true;
    }

    @Transactional
    public boolean unlike(Long postId, Long userId) {
        Post post = posts.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));
        users.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        boolean exists = likes.existsByUserIdAndPostId(userId, postId);
        if (!exists) {
            return false;
        }

        likes.deleteByUserIdAndPostId(userId, postId);

        post.decreaseLikesIfPossible();
        post.touchUpdatedAt(Instant.now());
        posts.save(post);

        return true;
    }

    public long count(Long postId) {
        return likes.countByPostId(postId);
    }

    // 상세조회에서 “현재 유저가 좋아요 눌렀는지 여부” 확인용
    public boolean isLiked(Long postId, Long userId) {
        return likes.existsByUserIdAndPostId(userId, postId);
    }
}
