package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.User;
import com.example.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public Map<Long, User> findByIds(Set<Long> ids) {
        Map<Long, User> result = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }

        // JPA에서 IN 쿼리 한 번
        for (User u : repo.findAllById(ids)) {
            if (u.getId() != null) {
                result.put(u.getId(), u);
            }
        }
        return result;
    }


    @Transactional
    public User signup(String email, String password, String nickname, String profileImage) {
        repo.findByEmail(email).ifPresent(u -> {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXIST);
        });

        User u = User.create(email, password, nickname, profileImage);
        return repo.save(u);
    }

    public User login(String email, String password) {
        User u = repo.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String savedPw = u.getPassword();
        if (savedPw == null || !savedPw.equals(password)) {
            throw new BusinessException(ErrorCode.LOGIN_PASSWORD_WRONG);
        }
        return u;
    }

    public User getMe(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public User updateMe(Long userId, String nickname, String profileImage) {
        User u = getMe(userId);
        u.updateProfile(nickname, profileImage);
        u.touchUpdatedAt(Instant.now());
        return repo.save(u);
    }

//    @Transactional
//    public void updatePassword(Long userId, String oldPw, String newPw) {
//        if (oldPw == null || newPw == null) {
//            throw new BusinessException(ErrorCode.BAD_REQUEST);
//        }
//
//        User u = getMe(userId);
//        String savedPw = u.getPassword();
//        if (savedPw == null || !savedPw.equals(oldPw)) {
//            throw new BusinessException(ErrorCode.LOGIN_PASSWORD_WRONG);
//        }
//
//        u.changePassword(newPw);
//        u.touchUpdatedAt(Instant.now());
//        repo.save(u);
//    }

    @Transactional
    public void updatePassword(Long userId, String newPw) {
        if (newPw == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        User u = getMe(userId);

        u.changePassword(newPw);
        u.touchUpdatedAt(Instant.now());
        repo.save(u);
    }

    @Transactional
    public void withdraw(Long userId) {
        getMe(userId);
        repo.deleteById(userId);
    }
}
