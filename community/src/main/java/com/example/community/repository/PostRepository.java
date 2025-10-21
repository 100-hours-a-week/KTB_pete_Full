package com.example.community.repository; // 레포지토리 패키지

import com.example.community.domain.Post; // 도메인 모델
import java.util.List; // 리스트 인터페이스
import java.util.Optional; // 값이 없을 수 있음

public interface PostRepository {
    Optional<Post> findById(Long id); // ID로 조회
    List<Post> findAll();             // 전체 조회
    Post save(Post post);             // 저장(신규/수정)
    void deleteById(Long id);         // 삭제
    long count();                     // 전체 개수
    long nextId();                    // 다음 ID 발급
}
