package com.example.community.domain; // 도메인/엔티티 패키지

import java.time.Instant; // UTC 타임스탬프 표현

/**
 * User 도메인 모델(엔티티 역할).
 * - DB가 없어도 서비스/레포지토리 간의 공용 데이터 구조로 사용한다.
 * - 컨트롤러 입출력에는 보안/계약 안정성을 위해 DTO를 사용한다.
 */
public class User {
    private Long id;                // 사용자 고유 ID
    private String email;           // 이메일(로그인 ID)
    private String password;        // 비밀번호(모의 환경: 평문. 실제 서비스에선 절대 평문 저장 금지)
    private String nickname;        // 닉네임
    private String profileImageUrl; // 프로필 이미지 URL
    private Instant createdAt;      // 생성 시각
    private Instant updatedAt;      // 수정 시각

    // 기본 생성자(역직렬화, 프레임워크용)
    public User() {}

    // 전체 필드를 초기화하는 생성자
    public User(Long id, String email, String password, String nickname,
                String profileImageUrl, Instant createdAt, Instant updatedAt) {
        this.id = id; // 각 필드 초기화
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 게터/세터들
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
