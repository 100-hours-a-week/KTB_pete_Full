package com.example.community.repository.memory;

import com.example.community.domain.User;
import com.example.community.repository.UserRepository;
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
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> store = new ConcurrentHashMap<Long, User>();
    private final AtomicLong seq = new AtomicLong(0L);

    public InMemoryUserRepository(ObjectMapper om) {
        try {
            ClassPathResource res = new ClassPathResource("data/users.json");
            if (res.exists()) {
                InputStream is = res.getInputStream();
                try {
                    TypeReference<List<User>> typeRef = new TypeReference<List<User>>() {};
                    List<User> users = om.readValue(is, typeRef);

                    if (users != null) {
                        int size = users.size();
                        int i = 0;
                        while (i < size) {
                            User u = users.get(i);
                            if (u != null) {
                                // 백필: timestamps
                                Instant now = Instant.now();
                                if (u.getCreatedAt() == null) { u.setCreatedAt(now); }
                                if (u.getUpdatedAt() == null) { u.setUpdatedAt(now); }

                                store.put(u.getId(), u);

                                long current = seq.get();
                                long idVal;
                                if (u.getId() == null) {
                                    idVal = 0L;
                                } else {
                                    idVal = u.getId().longValue();
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
            // 로딩 실패 시 빈 저장소로 시작
        }
    }

    public java.util.Optional<User> findById(Long id) {
        User found = store.get(id);
        return java.util.Optional.ofNullable(found);
    }

    public java.util.Optional<User> findByEmail(String email) {
        if (email == null) {
            return java.util.Optional.ofNullable(null);
        }

        List<User> values = new ArrayList<User>(store.values());
        int size = values.size();
        int i = 0;
        while (i < size) {
            User u = values.get(i);
            if (u != null) {
                String e = u.getEmail();
                if (e != null) {
                    if (e.equalsIgnoreCase(email)) {
                        return java.util.Optional.of(u);
                    }
                }
            }
            i = i + 1;
        }
        return java.util.Optional.ofNullable(null);
    }

    public List<User> findAll() {
        List<User> copy = new ArrayList<User>(store.values());
        return Collections.unmodifiableList(copy);
    }

    public User save(User user) {
        if (user == null) {
            return null;
        }
        Instant now = Instant.now();
        if (user.getId() == null) {
            long newId = nextId();
            user.setId(newId);
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(now);
            }
        }
        user.setUpdatedAt(now);
        store.put(user.getId(), user);
        return user;
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
