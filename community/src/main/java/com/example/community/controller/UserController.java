package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.TokenUtil;
import com.example.community.dto.user.UpdateMeRequest;
import com.example.community.dto.user.UpdatePasswordRequest;
import com.example.community.dto.user.UserResponse;
import com.example.community.domain.User;
import com.example.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = TokenUtil.resolveUserId(auth);
        User u = users.getMe(userId);

        String createdAtStr = (u.getCreatedAt() != null) ? u.getCreatedAt().toString() : "unknown";
        String updatedAtStr = (u.getUpdatedAt() != null) ? u.getUpdatedAt().toString() : "unknown";

        UserResponse response = new UserResponse(
                u.getId(), u.getEmail(), u.getNickname(),
                u.getProfileImageUrl(), createdAtStr, updatedAtStr
        );
        return ApiResponse.ok("유저 정보 조회 성공", response);
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateMe(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody UpdateMeRequest body
    ) {
        Long userId = TokenUtil.resolveUserId(auth);
        User u = users.updateMe(userId, body.nickname, body.profileImage);

        String createdAtStr = (u.getCreatedAt() != null) ? u.getCreatedAt().toString() : "unknown";
        String updatedAtStr = (u.getUpdatedAt() != null) ? u.getUpdatedAt().toString() : "unknown";

        UserResponse response = new UserResponse(
                u.getId(), u.getEmail(), u.getNickname(),
                u.getProfileImageUrl(), createdAtStr, updatedAtStr
        );
        return ApiResponse.ok("유저 정보 수정 성공", response);
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @jakarta.validation.Valid @RequestBody com.example.community.dto.user.UpdatePasswordRequest body
    ) {
        Long userId = com.example.community.common.TokenUtil.resolveUserId(auth);

        // 서비스 메서드가 updatePassword(oldPw, newPw)로 교체되어 있어야 함
        users.updatePassword(userId, body.oldPassword, body.newPassword);

        return ResponseEntity.ok(com.example.community.common.ApiResponse.ok("OK", "ok"));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = TokenUtil.resolveUserId(auth);
        users.withdraw(userId);
        return ApiResponse.ok("유저 탈퇴 성공", null);
    }
}
