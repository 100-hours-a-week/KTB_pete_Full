package com.example.community.controller;

import com.example.community.common.ApiResponse;
import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.common.web.CurrentUserId;
import com.example.community.dto.user.UpdatePasswordRequest;
import com.example.community.dto.user.UserResponse;
import com.example.community.domain.User;
import com.example.community.mapper.UserMapper;
import com.example.community.service.UserService;
import com.example.community.storage.FileStorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService users;
    private final FileStorageService fileStorageService;

    public UserController(UserService users, FileStorageService fileStorageService) {
        this.users = users;
        this.fileStorageService = fileStorageService;
    }

    // 내 정보 조회 (변경 없음)
    @GetMapping("/me")
    @io.swagger.v3.oas.annotations.Operation(summary = "내 정보 조회")
    public ApiResponse<UserResponse> me(@CurrentUserId Long userId) {
        User u = users.getMe(userId);
        return ApiResponse.ok("유저 정보 조회 성공", UserMapper.toResponse(u));
    }

    // 내 정보 수정 (multipart/form-data)
    @PatchMapping(path = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @io.swagger.v3.oas.annotations.Operation(summary = "내 정보 수정", description = "닉네임/프로필 이미지 수정")
    public ApiResponse<UserResponse> updateMe(
            @CurrentUserId Long userId,
            @RequestPart(value = "nickname", required = false) @Size(min = 1, max = 100) String nickname,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = fileStorageService.store(profileImage, "profiles");
        }

        User u = users.updateMe(userId, nickname, profileImageUrl);
        return ApiResponse.ok("유저 정보 수정 성공", UserMapper.toResponse(u));
    }

    // 비밀번호 변경
//    @PatchMapping("/me/password")
//    @io.swagger.v3.oas.annotations.Operation(summary = "비밀번호 변경", description = "구/신규 비밀번호 검증 실패 400 가능")
//    public ApiResponse<String> updatePassword(
//            @CurrentUserId Long userId,
//            @jakarta.validation.Valid @RequestBody com.example.community.dto.user.UpdatePasswordRequest body
//    ) {
//        users.updatePassword(userId, body.oldPassword, body.newPassword);
//        return ApiResponse.ok("OK", "ok");
//    }

    @PatchMapping("/me/password")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "비밀번호 변경",
            description = "신규 비밀번호/확인 값 검증 실패 시 400 반환"
    )
    public ApiResponse<String> updatePassword(
            @CurrentUserId Long userId,
            @Valid @RequestBody UpdatePasswordRequest body
    ) {
        // 1) null/blank 체크
        if (body.newPassword == null || body.confirmPassword == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        String newPw = body.newPassword.trim();
        String confirmPw = body.confirmPassword.trim();
        if (newPw.isEmpty() || confirmPw.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 2) 두 비밀번호가 서로 다른 경우
        if (!newPw.equals(confirmPw)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 3) 서비스 호출 (이제는 새 비밀번호만 넘김)
        users.updatePassword(userId, newPw);

        return ApiResponse.ok("비밀번호 변경 성공", "ok");
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    @io.swagger.v3.oas.annotations.Operation(summary = "회원 탈퇴")
    public ApiResponse<Void> withdraw(@CurrentUserId Long userId) {
        users.withdraw(userId);
        return ApiResponse.ok("유저 탈퇴 성공", null);
    }
}
