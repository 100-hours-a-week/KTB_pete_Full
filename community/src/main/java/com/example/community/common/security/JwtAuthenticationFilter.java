package com.example.community.common.security;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // /auth/**, /swagger, /uploads 등은 JWT 검사 스킵
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // 헤더가 없으면 -> 그냥 익명 요청으로 둔다 (Some API는 permitAll이므로)
        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String prefix = "Bearer ";
        String trimmed = authHeader.trim();
        if (!trimmed.startsWith(prefix)) {
            // 형식이 이상하면 우리 전역 예외 핸들러로 401 보내기
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }

        String token = trimmed.substring(prefix.length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }

        // 이미 인증이 설정된 경우는 건너뜀(중복 설정 방지)
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // 토큰에서 userId 추출 (유효성 검사는 JwtProvider에서 처리)
            Long userId = jwtProvider.extractUserId(token);

            // 간단하게 userId만 principal 로 넣는 Authentication 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.emptyList() // 권한은 지금 안 쓰므로 빈 리스트
                    );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        if (path == null) return true;
        // 인증 없이 접근 허용할 경로들
        return path.startsWith("/auth")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.startsWith("/uploads");
    }
}
