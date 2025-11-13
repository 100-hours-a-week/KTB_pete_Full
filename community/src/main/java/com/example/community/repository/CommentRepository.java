package com.example.community.repository;

import com.example.community.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostId(Long postId);

    Optional<Comment> findByIdAndPostId(Long id, Long postId);

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    long countByPostId(Long postId);
}
