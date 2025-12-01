package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.common.security.JwtProvider;
import com.example.community.common.web.AuthCookieUtil;
import com.example.community.dto.auth.LoginRequest;
import com.example.community.dto.auth.LoginResult;
import com.example.community.dto.auth.LoginUser;
import com.example.community.domain.User;
import com.example.community.service.RefreshTokenService;
import com.example.community.service.UserService;
import com.example.community.storage.FileStorageService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService users;
    private final FileStorageService fileStorageService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    // 로컬 개발 환경에서만 secure=false, 운영에서는 true로 바꾸거나 설정으로 빼도 됨
    private final boolean refreshTokenCookieSecure = false;

    public AuthController(UserService users,
                          FileStorageService fileStorageService,
                          JwtProvider jwtProvider,
                          RefreshTokenService refreshTokenService) {
        this.users = users;
        this.fileStorageService = fileStorageService;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    // 회원가입 (multipart/form-data, 프로필 이미지 파일 업로드)
    @PostMapping(
            value = "/signup",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @io.swagger.v3.oas.annotations.Operation(summary = "회원가입", description = "이메일 중복 시 409 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이미 사용 중인 이메일",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"isSuccess\":false,\"code\":409,\"message\":\"이미 사용 중인 이메일입니다.\",\"result\":null}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "검증 실패",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"isSuccess\":false,\"code\":400,\"message\":\"요청 값이 올바르지 않습니다.\",\"result\":null}"
                            )
                    )
            )
    })
    public ApiResponse<SignupResult> signup(
            @RequestPart("nickname") String nickname,
            @RequestPart("email") String email,
            @RequestPart("password") String password,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        // 1) 프로필 이미지 파일 저장 (선택)
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = fileStorageService.store(profileImage, "profiles");
        }

        // 2) 회원 가입 처리 (DB에는 URL 문자열만 저장)
        User u = users.signup(email, password, nickname, profileImageUrl);

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
    @io.swagger.v3.oas.annotations.Operation(summary = "로그인", description = "비밀번호 불일치 시 401 반환")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "비밀번호가 일치하지 않습니다.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "{\"isSuccess\":false,\"code\":401,\"message\":\"비밀번호가 일치하지 않습니다.\",\"result\":null}"
                            )
                    )
            )
    })
    public ApiResponse<LoginResult> login(
            @Valid @RequestBody LoginRequest body,
            HttpServletResponse response
    ) {
        // 1) 이메일/비밀번호 검증
        User u = users.login(body.email, body.password);

        // 2) Access Token 발급 (JWT)
        String accessToken = jwtProvider.createAccessToken(u.getId(), u.getEmail());

        // 3) Refresh Token 발급, DB 저장
        RefreshTokenService.RefreshTokenResult rt =
                refreshTokenService.issue(u.getId());

        // 4) Refresh Token을 HttpOnly 쿠키로 내려주기
        AuthCookieUtil.addRefreshTokenCookie(
                response,
                rt.token,
                refreshTokenCookieSecure
        );

        // 5) 응답 바디 구성 (FE 입장에서는 token = Access Token)
        LoginUser user = new LoginUser(
                String.valueOf(u.getId()),
                u.getEmail(),
                u.getNickname(),
                u.getProfileImageUrl()
        );
        LoginResult result = new LoginResult(accessToken, user);

        return ApiResponse.ok("로그인 성공", result);
    }

    // Refresh Token 기반 Access Token 재발급
    @PostMapping("/refresh")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Access Token 재발급",
            description = "유효한 Refresh Token 쿠키 기반으로 Access Token 재발급"
    )
    public ApiResponse<LoginResult> refresh(
            @CookieValue(value = AuthCookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false)
            String refreshTokenValue,
            HttpServletResponse response
    ) {
        if (refreshTokenValue == null || refreshTokenValue.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 1) RT 검증
        RefreshTokenService.RefreshTokenResult newRt =
                refreshTokenService.consumeAndReissue(refreshTokenValue);

        Long userId = newRt.userId;

        // 2) 유저 정보 조회 (이메일/닉네임 포함)
        User u = users.getMe(userId);

        // 3) Access Token 재발급
        String newAccessToken = jwtProvider.createAccessToken(u.getId(), u.getEmail());

        // 4) 새 Refresh Token을 다시 쿠키에 심기
        AuthCookieUtil.addRefreshTokenCookie(
                response,
                newRt.token,
                refreshTokenCookieSecure
        );

        // 5) 응답 구성 (로그인과 동일한 형태)
        LoginUser user = new LoginUser(
                String.valueOf(u.getId()),
                u.getEmail(),
                u.getNickname(),
                u.getProfileImageUrl()
        );
        LoginResult result = new LoginResult(newAccessToken, user);

        return ApiResponse.ok("토큰 재발급 성공", result);
    }

    // 로그아웃
    @PostMapping("/logout")
    @io.swagger.v3.oas.annotations.Operation(summary = "로그아웃")
    public ApiResponse<Void> logout(
            @CookieValue(value = AuthCookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false)
            String refreshTokenValue,
            HttpServletResponse response
    ) {
        // 1) RT가 있으면 DB에서 제거
        if (refreshTokenValue != null && !refreshTokenValue.trim().isEmpty()) {
            refreshTokenService.revokeByToken(refreshTokenValue);
        }

        // 2) 클라이언트 쿠키 제거
        AuthCookieUtil.clearRefreshTokenCookie(response, refreshTokenCookieSecure);

        return ApiResponse.ok("로그아웃 성공", null);
    }

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
