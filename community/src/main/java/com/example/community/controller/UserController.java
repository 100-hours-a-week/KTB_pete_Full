package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.web.CurrentUserId;
import com.example.community.dto.user.UpdateMeRequest;
import com.example.community.dto.user.UserResponse;
import com.example.community.domain.User;
import com.example.community.mapper.UserMapper;
import com.example.community.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    // 내 정보 조회
    @GetMapping("/me")
    @io.swagger.v3.oas.annotations.Operation(summary = "내 정보 조회")
    public ApiResponse<UserResponse> me(@CurrentUserId Long userId) {
        User u = users.getMe(userId);
        return ApiResponse.ok("유저 정보 조회 성공", UserMapper.toResponse(u));
    }

    // 내 정보 수정
    @PatchMapping("/me")
    @io.swagger.v3.oas.annotations.Operation(summary = "내 정보 수정", description = "검증 실패 400 가능")
    public ApiResponse<UserResponse> updateMe(
            @CurrentUserId Long userId,
            @RequestBody UpdateMeRequest body
    ) {
        User u = users.updateMe(userId, body.nickname, body.profileImage);
        return ApiResponse.ok("유저 정보 수정 성공", UserMapper.toResponse(u));
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    @io.swagger.v3.oas.annotations.Operation(summary = "비밀번호 변경", description = "구/신규 비밀번호 검증 실패 400 가능")
    public ApiResponse<String> updatePassword(
            @CurrentUserId Long userId,
            @jakarta.validation.Valid @RequestBody com.example.community.dto.user.UpdatePasswordRequest body
    ) {
        users.updatePassword(userId, body.oldPassword, body.newPassword);
        return ApiResponse.ok("OK", "ok");
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    @io.swagger.v3.oas.annotations.Operation(summary = "회원 탈퇴")
    public ApiResponse<Void> withdraw(@CurrentUserId Long userId) {
        users.withdraw(userId);
        return ApiResponse.ok("유저 탈퇴 성공", null);
    }
}
