package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.security.TokenUtil;
import com.example.community.dto.auth.LoginRequest;
import com.example.community.dto.auth.LoginResult;
import com.example.community.dto.auth.LoginUser;
import com.example.community.domain.User;
import com.example.community.service.UserService;
import com.example.community.storage.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService users;
    private final FileStorageService fileStorageService;

    public AuthController(UserService users, FileStorageService fileStorageService) {
        this.users = users;
        this.fileStorageService = fileStorageService;
    }

    // 회원가입 (multipart/form-data + 프로필 이미지 파일 업로드)
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
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest body) {
        User u = users.login(body.email, body.password);
        String token = TokenUtil.issueDummyToken(u.getId());

        // 🔧 닉네임 + 프로필 이미지까지 포함해서 내려주기
        LoginUser user = new LoginUser(
                String.valueOf(u.getId()),
                u.getEmail(),
                u.getNickname(),
                u.getProfileImageUrl()
        );

        LoginResult result = new LoginResult(token, user);
        return ApiResponse.ok("로그인 성공", result);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
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
