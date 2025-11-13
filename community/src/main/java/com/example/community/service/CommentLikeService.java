package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.CommentLike;
import com.example.community.repository.CommentLikeRepository;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository likes;
    private final CommentRepository comments;
    private final UserRepository users;

    public CommentLikeService(CommentLikeRepository likes, CommentRepository comments, UserRepository users) {
        this.likes = likes;
        this.comments = comments;
        this.users = users;
    }

    @Transactional
    public boolean like(Long commentId, Long userId) {
        comments.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));
        users.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        boolean exists = likes.existsByUserIdAndCommentId(userId, commentId);
        if (exists) {
            return false;
        }
        CommentLike cl = CommentLike.create(commentId, userId);
        likes.save(cl);
        return true;
    }

    @Transactional
    public boolean unlike(Long commentId, Long userId) {
        comments.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));
        users.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        boolean exists = likes.existsByUserIdAndCommentId(userId, commentId);
        if (!exists) {
            return false;
        }
        likes.deleteByUserIdAndCommentId(userId, commentId);
        return true;
    }

    public long count(Long commentId) {
        return likes.countByCommentId(commentId);
    }

    // N+1 방지 여러 댓글에 대한 좋아요 수를 Map으로 반환
    public Map<Long, Long> countByCommentIds(List<Long> commentIds) {
        Map<Long, Long> result = new HashMap<Long, Long>();
        if (commentIds == null || commentIds.isEmpty()) {
            return result;
        }

        List<CommentLikeRepository.CommentLikeCount> rows =
                likes.countByCommentIdIn(commentIds);

        int size = rows.size();
        int i = 0;
        while (i < size) {
            CommentLikeRepository.CommentLikeCount row = rows.get(i);
            Long cid = row.getCommentId();
            Long cnt = row.getCnt();
            if (cid != null && cnt != null) {
                result.put(cid, cnt);
            }
            i = i + 1;
        }
        return result;
    }
}
