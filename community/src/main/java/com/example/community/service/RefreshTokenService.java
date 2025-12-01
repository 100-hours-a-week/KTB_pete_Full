package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.RefreshToken;
import com.example.community.domain.User;
import com.example.community.repository.RefreshTokenRepository;
import com.example.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokens;
    private final UserRepository users;
    private final SecureRandom secureRandom = new SecureRandom();

    // RT 유효 기간: 30일
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    public RefreshTokenService(RefreshTokenRepository refreshTokens,
                               UserRepository users) {
        this.refreshTokens = refreshTokens;
        this.users = users;
    }

    // 유저에게 새로운 Refresh Token 발급
    @Transactional
    public RefreshTokenResult issue(Long userId) {
        // 유저 존재 여부 검증
        Optional<User> userOpt = users.findById(userId);
        if (userOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 유저당 하나의 RT만 유지
        refreshTokens.deleteByUserId(userId);

        String tokenValue = generateTokenValue();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(REFRESH_TOKEN_TTL);

        RefreshToken entity = RefreshToken.create(userId, tokenValue, expiresAt);
        RefreshToken saved = refreshTokens.save(entity);

        return new RefreshTokenResult(saved.getUserId(), saved.getToken(), saved.getExpiresAt());
    }

    // 주어진 토큰 문자열이 유효한지 검증하고, 유저 ID를 반환
    public RefreshTokenResult validate(String tokenValue) {
        if (tokenValue == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String trimmed = tokenValue.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        RefreshToken token = refreshTokens.findByToken(trimmed)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        Instant now = Instant.now();
        if (token.isExpired(now)) {
            // 만료된 토큰은 정리
            refreshTokens.deleteById(token.getId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return new RefreshTokenResult(token.getUserId(), token.getToken(), token.getExpiresAt());
    }

    // 재발급용 메서드
    @Transactional
    public RefreshTokenResult consumeAndReissue(String oldTokenValue) {
        RefreshTokenResult current = validate(oldTokenValue);
        Long userId = current.userId;

        // 혹시 남아 있을 수 있는 기존 토큰 정리
        refreshTokens.deleteByUserId(userId);

        return issue(userId);
    }

    // 유저 기준 토큰 폐기 (로그아웃 시 사용)
    @Transactional
    public void revokeByUserId(Long userId) {
        refreshTokens.deleteByUserId(userId);
    }

    // 토큰 문자열 기준 폐기 (쿠키 값이 있을 때 직접 삭제하고 싶을 때 사용)
    @Transactional
    public void revokeByToken(String tokenValue) {
        if (tokenValue == null) {
            return;
        }
        String trimmed = tokenValue.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        Optional<RefreshToken> tokenOpt = refreshTokens.findByToken(trimmed);
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            refreshTokens.deleteById(token.getId());
        }
    }

    // RT 문자열 생성 로직
    private String generateTokenValue() {
        byte[] bytes = new byte[64]; // 512bit
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // 서비스 외부로 내보낼 결과 DTO
    public static class RefreshTokenResult {
        public final Long userId;
        public final String token;
        public final Instant expiresAt;

        public RefreshTokenResult(Long userId, String token, Instant expiresAt) {
            this.userId = userId;
            this.token = token;
            this.expiresAt = expiresAt;
        }
    }
}
