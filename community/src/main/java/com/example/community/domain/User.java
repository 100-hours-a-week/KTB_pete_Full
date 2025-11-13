package com.example.community.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    private User(Long id, String email, String password, String nickname,
                 String profileImageUrl, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User create(String email, String password, String nickname, String profileImageUrl) {
        Instant now = Instant.now();
        return new User(null, email, password, nickname, profileImageUrl, now, now);
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null) {
            String t = nickname.trim();
            if (!t.isEmpty()) {
                this.nickname = t;
            }
        }
        if (profileImageUrl != null) {
            String p = profileImageUrl.trim();
            if (!p.isEmpty()) {
                this.profileImageUrl = p;
            }
        }
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void touchUpdatedAt(Instant now) {
        this.updatedAt = now;
    }

    // 게터들
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
