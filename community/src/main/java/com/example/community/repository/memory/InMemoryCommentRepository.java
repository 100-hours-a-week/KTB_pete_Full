package com.example.community.repository.memory;

import com.example.community.domain.Comment;
import com.example.community.repository.CommentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryCommentRepository implements CommentRepository {

    private final Map<Long, Comment> store = new ConcurrentHashMap<Long, Comment>();
    private final AtomicLong seq = new AtomicLong(0L);

    public InMemoryCommentRepository(ObjectMapper om) {
        try {
            ClassPathResource res = new ClassPathResource("data/comments.json");
            if (res.exists()) {
                InputStream is = res.getInputStream();
                try {
                    TypeReference<java.util.List<Comment>> typeRef = new TypeReference<java.util.List<Comment>>() {};
                    java.util.List<Comment> comments = om.readValue(is, typeRef);
                    if (comments != null) {
                        int size = comments.size();
                        int i = 0;
                        while (i < size) {
                            Comment c = comments.get(i);
                            if (c != null) {
                                // 백필: timestamps
                                Instant now = Instant.now();
                                if (c.getCreatedAt() == null) { c.setCreatedAt(now); }
                                if (c.getUpdatedAt() == null) { c.setUpdatedAt(now); }

                                store.put(c.getId(), c);

                                long current = seq.get();
                                long idVal;
                                if (c.getId() == null) {
                                    idVal = 0L;
                                } else {
                                    idVal = c.getId().longValue();
                                }
                                if (idVal > current) {
                                    seq.set(idVal);
                                }
                            }
                            i = i + 1;
                        }
                    }
                } finally {
                    is.close();
                }
            }
        } catch (Exception e) {
            // 로딩 실패는 무시
        }
    }

    public java.util.Optional<Comment> findById(Long id) {
        Comment c = store.get(id);
        return java.util.Optional.ofNullable(c);
    }

    public java.util.List<Comment> findByPostId(Long postId) {
        java.util.List<Comment> values = new java.util.ArrayList<Comment>(store.values());
        java.util.List<Comment> result = new java.util.ArrayList<Comment>();
        int size = values.size();
        int i = 0;
        while (i < size) {
            Comment c = values.get(i);
            if (c != null) {
                Long pid = c.getPostId();
                if (pid != null) {
                    if (postId != null) {
                        if (pid.equals(postId)) {
                            result.add(c);
                        }
                    }
                }
            }
            i = i + 1;
        }
        return result;
    }

    public Comment save(Comment comment) {
        if (comment == null) {
            return null;
        }
        Instant now = Instant.now();
        if (comment.getId() == null) {
            long newId = nextId();
            comment.setId(newId);
            if (comment.getCreatedAt() == null) {
                comment.setCreatedAt(now);
            }
        }
        comment.setUpdatedAt(now);
        store.put(comment.getId(), comment);
        return comment;
    }

    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        store.remove(id);
    }

    public long nextId() {
        return seq.incrementAndGet();
    }
}
