package com.example.community.repository.memory;

import com.example.community.domain.Post;
import com.example.community.repository.PostRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPostRepository implements PostRepository {

    private final Map<Long, Post> store = new ConcurrentHashMap<Long, Post>();
    private final AtomicLong seq = new AtomicLong(0L);

    public InMemoryPostRepository(ObjectMapper om) {
        try {
            ClassPathResource res = new ClassPathResource("data/posts.json");
            if (res.exists()) {
                InputStream is = res.getInputStream();
                try {
                    TypeReference<List<Post>> typeRef = new TypeReference<List<Post>>() {};
                    List<Post> posts = om.readValue(is, typeRef);
                    if (posts != null) {
                        int size = posts.size();
                        int i = 0;
                        while (i < size) {
                            Post p = posts.get(i);
                            if (p != null) {
                                // --- 백필(backfill) 시작 ---
                                // image
                                if (p.getImage() == null) {
                                    p.setImage("");
                                }
                                // counters
                                if (p.getComments() == null) { p.setComments(0L); }
                                if (p.getLikes() == null)    { p.setLikes(0L); }
                                if (p.getViews() == null)    { p.setViews(0L); }
                                // timestamps
                                Instant now = Instant.now();
                                if (p.getCreatedAt() == null) { p.setCreatedAt(now); }
                                if (p.getUpdatedAt() == null) { p.setUpdatedAt(now); }
                                // --- 백필(backfill) 끝 ---

                                store.put(p.getId(), p);

                                long current = seq.get();
                                long idVal;
                                if (p.getId() == null) {
                                    idVal = 0L;
                                } else {
                                    idVal = p.getId().longValue();
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
            // 로딩 실패 시 무시(실무: 로깅 권장)
        }
    }

    public java.util.Optional<Post> findById(Long id) {
        Post p = store.get(id);
        return java.util.Optional.ofNullable(p);
    }

    public List<Post> findAll() {
        List<Post> copy = new ArrayList<Post>(store.values());
        return Collections.unmodifiableList(copy);
    }

    public Post save(Post post) {
        if (post == null) {
            return null;
        }

        // 안전 보정 (서비스에서 null로 보낼 가능성 대비)
        if (post.getImage() == null)  { post.setImage(""); }
        if (post.getComments() == null) { post.setComments(0L); }
        if (post.getLikes() == null)    { post.setLikes(0L); }
        if (post.getViews() == null)    { post.setViews(0L); }

        Instant now = Instant.now();
        if (post.getId() == null) {
            long newId = nextId();
            post.setId(newId);
            if (post.getCreatedAt() == null) {
                post.setCreatedAt(now);
            }
        }
        post.setUpdatedAt(now);
        store.put(post.getId(), post);
        return post;
    }

    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        store.remove(id);
    }

    public long count() {
        return store.size();
    }

    public long nextId() {
        return seq.incrementAndGet();
    }
}
