package com.example.community.repository; // 레포지토리 패키지

import com.example.community.domain.User; // 도메인 모델
import java.util.List; // 리스트 타입
import java.util.Optional; // 값이 없을 수 있음

public interface UserRepository {
    Optional<User> findById(Long id);     // ID로 조회
    Optional<User> findByEmail(String email); // 이메일로 조회
    List<User> findAll();                 // 전체 조회
    User save(User user);                 // 저장(신규/수정)
    void deleteById(Long id);             // ID로 삭제
    long nextId();                        // 다음 ID 발급
}
