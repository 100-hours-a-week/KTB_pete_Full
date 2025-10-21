package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.TokenUtil;
import com.example.community.dto.auth.LoginRequest;
import com.example.community.dto.auth.LoginResult;
import com.example.community.dto.auth.LoginUser;
import com.example.community.dto.auth.SignupRequest;
import com.example.community.domain.User;
import com.example.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<SignupResult> signup(@Valid @RequestBody SignupRequest body) {
        // profileImage까지 전달
        User u = users.signup(body.email, body.password, body.nickname, body.profileImage);

        SignupResult result = new SignupResult(
                String.valueOf(u.getId()),
                u.getNickname(),
                u.getEmail(),
                u.getProfileImageUrl()
        );
        return ApiResponse.ok("회원 가입이 성공적으로 완료되었습니다.", result);
    }

    // 로그인
    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest body) {
        User u = users.login(body.email, body.password);
        String token = TokenUtil.issueDummyToken(u.getId());

        LoginUser user = new LoginUser(String.valueOf(u.getId()), u.getEmail(), "active");
        LoginResult result = new LoginResult(token, user);
        return ApiResponse.ok("로그인 성공", result);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok("로그아웃 성공", null);
    }

    // 응답 전용
    static class SignupResult {
        public final String id;
        public final String nickname;
        public final String email;
        public final String profileImage;
        SignupResult(String id, String nickname, String email, String profileImage) {
            this.id = id; this.nickname = nickname; this.email = email; this.profileImage = profileImage;
        }
    }
}
