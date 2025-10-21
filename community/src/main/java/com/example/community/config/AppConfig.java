package com.example.community.config; // 설정 패키지

// JSON 직렬화/역직렬화 도구
import com.fasterxml.jackson.databind.ObjectMapper;
// 직렬화 옵션
import com.fasterxml.jackson.databind.SerializationFeature;
// 스프링 빈 등록
import org.springframework.context.annotation.Bean;
// 설정 클래스 표시
import org.springframework.context.annotation.Configuration;

// 스프링 설정 클래스로 인식
@Configuration
public class AppConfig {
    // 스프링 컨테이너가 관리하는 Bean으로 등록
    @Bean
    public ObjectMapper objectMapper() {
        // 객체 생성
        ObjectMapper om = new ObjectMapper();
        // 날짜를 숫자 대신 문자열로
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return om;
    }
}
