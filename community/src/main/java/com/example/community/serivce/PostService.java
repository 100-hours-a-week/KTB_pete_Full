package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.repository.PostRepository;
import com.example.community.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class PostService {

    private final PostRepository posts;
    private final UserRepository users;

    public PostService(PostRepository posts, UserRepository users) {
        this.posts = posts;
        this.users = users;
    }

    // 게시글 생성 (image 반영, 카운터 초기화, 타임스탬프 세팅)
    public Post create(Long authorId, String title, String content, String image) {
        java.util.Optional<User> opt = users.findById(authorId);
        if (opt == null || !opt.isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (title == null || title.trim().length() == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (content == null || content.trim().length() == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Instant now = Instant.now();
        Post p = new Post(
                null,
                authorId,
                title,
                content,
                image,          // image
                0L,             // comments
                0L,             // likes (좋아요 기능은 이번 스레드에서 비활성, 카운터만 유지)
                0L,             // views
                now,            // createdAt
                now             // updatedAt
        );
        return posts.save(p);
    }

    // 단건 조회 (조회수 +1 반영)
    public Post getOne(Long postId) {
        java.util.Optional<Post> opt = posts.findById(postId);
        if (opt == null || !opt.isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        Post p = opt.get();
        Long v = p.getViews();
        if (v == null) { v = 0L; }
        p.setViews(v + 1L);
        p.setUpdatedAt(Instant.now()); // 명세 샘플 응답에서 updatedAt이 함께 움직여도 무방
        return posts.save(p);
    }

    // 삭제 (작성자만)
    public void delete(Long postId, Long requesterId) {
        Post p = getOneWithoutTouch(postId); // 조회수 증가 없이 존재 확인용
        Long authorId = p.getAuthorId();
        if (authorId == null || !authorId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        posts.deleteById(postId);
    }

    // 수정 (image 포함, updatedAt 갱신)
    public Post update(Long postId, Long requesterId, String newTitle, String newContent, String newImage) {
        Post p = getOneWithoutTouch(postId); // 수정은 조회수 영향 X

        if (p.getAuthorId() == null || !p.getAuthorId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (newTitle != null) {
            if (newTitle.trim().length() > 0) {
                p.setTitle(newTitle);
            }
        }
        if (newContent != null) {
            if (newContent.trim().length() > 0) {
                p.setContent(newContent);
            }
        }
        if (newImage != null) {
            if (newImage.trim().length() > 0) {
                p.setImage(newImage);
            }
        }

        p.setUpdatedAt(Instant.now());
        return posts.save(p);
    }

    // 내부용: 조회수 증가 없이 조회
    private Post getOneWithoutTouch(Long postId) {
        java.util.Optional<Post> opt = posts.findById(postId);
        if (opt == null || !opt.isPresent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return opt.get();
    }

    // 목록 (페이지/정렬) - 기존 로직 유지
    public List<Post> listAllSortedPaged(int page, int size, String sort, String dir) {
        if (page < 0) { page = 0; }
        if (size <= 0) { size = 10; }

        List<Post> all = posts.findAll();
        List<Post> list = new ArrayList<Post>(all);

        Comparator<Post> comparator = new Comparator<Post>() {
            public int compare(Post a, Post b) {
                String sortKey = sort;
                if (sortKey == null) {
                    sortKey = "createdAt";
                }
                String direction = dir;
                if (direction == null) {
                    direction = "desc";
                }

                if ("createdAt".equalsIgnoreCase(sortKey)) {
                    long av;
                    Instant ai = a.getCreatedAt();
                    if (ai == null) { av = 0L; } else { av = ai.toEpochMilli(); }
                    long bv;
                    Instant bi = b.getCreatedAt();
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

                if ("title".equalsIgnoreCase(sortKey)) {
                    String at = a.getTitle();
                    if (at == null) { at = ""; }
                    String bt = b.getTitle();
                    if (bt == null) { bt = ""; }
                    int cmp = at.compareToIgnoreCase(bt);
                    if ("asc".equalsIgnoreCase(direction)) {
                        return cmp;
                    } else {
                        return -cmp;
                    }
                }

                long av;
                Instant ai = a.getCreatedAt();
                if (ai == null) { av = 0L; } else { av = ai.toEpochMilli(); }
                long bv;
                Instant bi = b.getCreatedAt();
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

        List<Post> paged = new ArrayList<Post>();
        int i = from;
        while (i < to) {
            paged.add(list.get(i));
            i = i + 1;
        }
        return paged;
    }

    public long countAll() {
        return posts.count();
    }
}
