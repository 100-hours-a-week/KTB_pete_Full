package com.example.community;

// 스프링 실행 할때 필요한 import
import org.springframework.boot.SpringApplication;
// 자동 설정 및 여러 컴포넌트들 스캔도 해줌
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
