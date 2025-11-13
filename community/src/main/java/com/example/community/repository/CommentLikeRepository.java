package com.example.community.repository;

import com.example.community.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    long countByCommentId(Long commentId);

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    // N+1 방지 여러 댓글에 대한 좋아요 수를 한 번에 조회
    @Query("select cl.commentId as commentId, count(cl) as cnt " +
            "from CommentLike cl " +
            "where cl.commentId in :commentIds " +
            "group by cl.commentId")
    List<CommentLikeCount> countByCommentIdIn(@Param("commentIds") List<Long> commentIds);

    interface CommentLikeCount {
        Long getCommentId();
        Long getCnt();
    }
}
