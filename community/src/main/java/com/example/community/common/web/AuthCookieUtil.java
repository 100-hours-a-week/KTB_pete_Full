package com.example.community.common.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

public final class AuthCookieUtil {

    // Refresh Token
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    // 30일제한
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private AuthCookieUtil() {
    }

    // Refresh Token을 HttpOnly 쿠키로 설정
    public static void addRefreshTokenCookie(HttpServletResponse response, String tokenValue, boolean secure) {
        if (tokenValue == null) {
            return;
        }

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokenValue)
                .httpOnly(true)
                .secure(secure)        // 로컬 개발에서는 false, 실제 배포에서는 true 권장
                .path("/")             // 전체 경로에 대해 전송
                .maxAge(REFRESH_TOKEN_TTL)
                .sameSite("Lax")       // CSRF 어느 정도 방어 + UX 균형
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    //Refresh Token 쿠키 삭제
    public static void clearRefreshTokenCookie(HttpServletResponse response, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)             // 즉시 만료
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
