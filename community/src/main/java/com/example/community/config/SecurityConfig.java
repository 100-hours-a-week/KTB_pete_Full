package com.example.community.config;

import com.example.community.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // REST API라 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 세션을 사용하지 않는 JWT 스타일
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 기본 로그인폼/HTTP Basic 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 요청별 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // 완전 공개 경로
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/uploads/**"
                        ).permitAll()

                        // 게시글/댓글 GET 조회는 비로그인 허용
                        .requestMatchers(HttpMethod.GET, "/board/**").permitAll()

                        // 그 외는 모두 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
