package com.example.community.common.security;

public interface JwtProvider {

    // 주어진 유저 정보 기반으로 Access Token(JWT) 생성
    String createAccessToken(Long userId, String email);

    // Access Token에서 userId 추출
    Long extractUserId(String accessToken);
}
