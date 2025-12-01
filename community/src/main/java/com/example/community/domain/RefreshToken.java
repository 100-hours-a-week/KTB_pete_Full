package com.example.community.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 실제 Refresh Token 문자열 (JWT)
    @Column(name = "token", nullable = false, length = 500, unique = true)
    private String token;

    // 만료 시각 (JWT exp와 동일/연동)
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RefreshToken() {
    }

    private RefreshToken(Long id, Long userId, String token, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static RefreshToken create(Long userId, String token, Instant expiresAt) {
        Instant now = Instant.now();
        return new RefreshToken(null, userId, token, expiresAt, now);
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isExpired(Instant now) {
        if (expiresAt == null) {
            return true;
        }
        return !now.isBefore(expiresAt);
    }
}
