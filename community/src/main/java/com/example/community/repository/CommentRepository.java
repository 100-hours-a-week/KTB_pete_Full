package com.example.community.repository;

import com.example.community.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Optional<Comment> findById(Long id);       // ID로 단건 조회
    List<Comment> findByPostId(Long postId);   // 게시글의 모든 댓글
    Comment save(Comment comment);             // 저장(신규/수정)
    void deleteById(Long id);                  // 삭제
    long nextId();                             // 다음 ID 발급
}
