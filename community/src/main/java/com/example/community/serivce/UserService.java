package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.User;
import com.example.community.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    // 회원가입: profileImage 추가, ErrorCode 키 복수형으로 수정
    public User signup(String email, String password, String nickname, String profileImage) {
        java.util.Optional<User> existedOpt = repo.findByEmail(email);
        if (existedOpt != null && existedOpt.isPresent()) {
            // ErrorCode.java 원본에 맞춰 복수형 사용
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXIST);

        }

        Instant now = Instant.now();
        // User( id, email, password, nickname, profileImageUrl, createdAt, updatedAt )
        User u = new User(null, email, password, nickname, profileImage, now, now);
        return repo.save(u);
    }

    // 로그인
    public User login(String email, String password) {
        java.util.Optional<User> foundOpt = repo.findByEmail(email);
        // ErrorCode 원본 기준: 없는 이메일이면 MEMBER_NOT_FOUND, 비번 틀리면 UNAUTHORIZED 사용 가능
        if (foundOpt == null || !foundOpt.isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        User u = foundOpt.get();
        String savedPw = u.getPassword();
        if (savedPw == null) {
            throw new BusinessException(ErrorCode.LOGIN_PASSWORD_WRONG);
        }
        if (!savedPw.equals(password)) {
            throw new BusinessException(ErrorCode.LOGIN_PASSWORD_WRONG);
        }
        return u;
    }

    // 내 정보 조회
    public User getMe(Long userId) {
        java.util.Optional<User> opt = repo.findById(userId);
        if (opt == null || !opt.isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return opt.get();
    }

    // 내 정보 수정
    public User updateMe(Long userId, String nickname, String profileImage) {
        User u = getMe(userId);

        if (nickname != null) {
            if (nickname.trim().length() > 0) {
                u.setNickname(nickname);
            }
        }
        if (profileImage != null) {
            if (profileImage.trim().length() > 0) {
                u.setProfileImageUrl(profileImage);
            }
        }

        u.setUpdatedAt(Instant.now());
        return repo.save(u);
    }

    // 비밀번호 변경
    public void updatePassword(Long userId, String oldPw, String newPw) {
        if (oldPw == null || newPw == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        User u = getMe(userId);
        String savedPw = u.getPassword();
        if (savedPw == null || !savedPw.equals(oldPw)) {
            throw new BusinessException(ErrorCode.LOGIN_PASSWORD_WRONG); // 401 비밀번호 불일치
        }

        u.setPassword(newPw);
        u.setUpdatedAt(java.time.Instant.now());
        repo.save(u);
    }



    // 회원 탈퇴
    public void withdraw(Long userId) {
        getMe(userId);
        repo.deleteById(userId);
    }
}
