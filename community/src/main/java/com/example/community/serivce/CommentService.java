package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.Comment;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.repository.CommentRepository;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository comments;
    private final PostRepository posts;
    private final UserRepository users;

    public CommentService(CommentRepository comments, PostRepository posts, UserRepository users) {
        this.comments = comments;
        this.posts = posts;
        this.users = users;
    }

    // 댓글 생성 (Post.comments ++, 타임스탬프 세팅)
    public Comment create(Long postId, Long authorId, String content) {
        java.util.Optional<Post> pOpt = posts.findById(postId);
        if (pOpt == null || !pOpt.isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        java.util.Optional<User> uOpt = users.findById(authorId);
        if (uOpt == null || !uOpt.isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (content == null || content.trim().length() == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Instant now = Instant.now();
        Comment c = new Comment(null, postId, authorId, content, now, now);
        Comment saved = comments.save(c);

        // 게시글 댓글 수 증가
        Post post = pOpt.get();
        Long cnt = post.getComments();
        if (cnt == null) { cnt = 0L; }
        post.setComments(cnt + 1L);
        post.setUpdatedAt(now);
        posts.save(post);

        return saved;
    }

    public Comment getOne(Long commentId) {
        java.util.Optional<Comment> opt = comments.findById(commentId);
        if (opt == null || !opt.isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return opt.get();
    }

    // 수정 (updatedAt 갱신)
    public Comment update(Long commentId, Long requesterId, String newContent) {
        Comment c = getOne(commentId);
        if (c.getAuthorId() == null || !c.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (newContent != null) {
            if (newContent.trim().length() > 0) {
                c.setContent(newContent);
            }
        }
        c.setUpdatedAt(Instant.now());
        return comments.save(c);
    }

    // 삭제 (Post.comments --)
    public void delete(Long commentId, Long requesterId) {
        Comment c = getOne(commentId);
        if (c.getAuthorId() == null || !c.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        comments.deleteById(commentId);

        java.util.Optional<Post> pOpt = posts.findById(c.getPostId());
        if (pOpt != null && pOpt.isPresent()) {
            Post p = pOpt.get();
            Long cnt = p.getComments();
            if (cnt == null) { cnt = 0L; }
            if (cnt > 0L) { p.setComments(cnt - 1L); }
            p.setUpdatedAt(Instant.now());
            posts.save(p);
        }
    }

    // 목록 (페이지/정렬) - 기존 유지
    public List<Comment> listByPostPaged(Long postId, int page, int size, String sort, String dir) {
        if (page < 0) { page = 0; }
        if (size <= 0) { size = 10; }

        List<Comment> all = comments.findByPostId(postId);
        List<Comment> list = new ArrayList<Comment>(all);

        Comparator<Comment> comparator = new Comparator<Comment>() {
            public int compare(Comment a, Comment b) {
                String sortKey = sort;
                if (sortKey == null) { sortKey = "createdAt"; }
                String direction = dir;
                if (direction == null) { direction = "desc"; }

                if ("createdAt".equalsIgnoreCase(sortKey)) {
                    long av;
                    java.time.Instant ai = a.getCreatedAt();
                    if (ai == null) { av = 0L; } else { av = ai.toEpochMilli(); }
                    long bv;
                    java.time.Instant bi = b.getCreatedAt();
                    if (bi == null) { bv = 0L; } else { bv = bi.toEpochMilli(); }

                    if ("asc".equalsIgnoreCase(direction)) {
                        if (av < bv) return -1;
                        if (av > bv) return 1;
                        return 0;
                    } else {
                        if (av > bv) return -1;
                        if (av < bv) return 1;
                        return 0;
                    }
                }

                long av;
                java.time.Instant ai = a.getCreatedAt();
                if (ai == null) { av = 0L; } else { av = ai.toEpochMilli(); }
                long bv;
                java.time.Instant bi = b.getCreatedAt();
                if (bi == null) { bv = 0L; } else { bv = bi.toEpochMilli(); }

                if ("asc".equalsIgnoreCase(direction)) {
                    if (av < bv) return -1;
                    if (av > bv) return 1;
                    return 0;
                } else {
                    if (av > bv) return -1;
                    if (av < bv) return 1;
                    return 0;
                }
            }
        };
        Collections.sort(list, comparator);

        int total = list.size();
        int from = page * size;
        if (from > total) { from = total; }
        int to = from + size;
        if (to > total) { to = total; }

        List<Comment> paged = new ArrayList<Comment>();
        int i = from;
        while (i < to) {
            paged.add(list.get(i));
            i = i + 1;
        }
        return paged;
    }

    public long countByPost(Long postId) {
        List<Comment> all = comments.findByPostId(postId);
        long count = 0L;
        int size = all.size();
        int i = 0;
        while (i < size) {
            if (all.get(i) != null) { count = count + 1L; }
            i = i + 1;
        }
        return count;
    }
}
