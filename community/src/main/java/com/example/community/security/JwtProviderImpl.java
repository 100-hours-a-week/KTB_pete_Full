package com.example.community.common.security;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProviderImpl implements JwtProvider {

    private final String issuer;
    private final SecretKey accessTokenKey;
    private final long accessTokenExpirationSeconds;

    public JwtProviderImpl(
            @Value("${app.jwt.issuer:community-api}") String issuer,
            @Value("${app.jwt.access-token-secret}") String accessTokenSecret,
            @Value("${app.jwt.access-token-exp-seconds:1800}") long accessTokenExpirationSeconds
    ) {
        this.issuer = issuer;

        if (accessTokenSecret == null) {
            throw new IllegalArgumentException("app.jwt.access-token-secret 설정이 필요합니다.");
        }

        byte[] keyBytes = accessTokenSecret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenKey = Keys.hmacShaKeyFor(keyBytes);

        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    @Override
    public String createAccessToken(Long userId, String email) {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 null일 수 없습니다.");
        }

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpirationSeconds);

        Date issuedAt = Date.from(now);
        Date expiration = Date.from(expiry);

        String subject = userId.toString();

        return Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .claim("email", email) // null이어도 괜찮음
                .signWith(accessTokenKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public Long extractUserId(String accessToken) {
        if (accessToken == null) {
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }
        String token = accessToken.trim();
        if (token.isEmpty()) {
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(accessTokenKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // issuer 검증 (선택적이지만, 발급자 체크하는 게 안전)
            String tokenIssuer = claims.getIssuer();
            if (tokenIssuer == null || !tokenIssuer.equals(issuer)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }

            String subject = claims.getSubject();
            if (subject == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }

            return Long.parseLong(subject);
        } catch (JwtException ex) {
            // 서명 불일치, 만료, 구조 오류 등
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
