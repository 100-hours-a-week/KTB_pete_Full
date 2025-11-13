package com.example.community.health;

import com.example.community.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// db 연결 확인용 코드
@RestController
public class DbHealthController {

    private final JdbcTemplate jdbc;

    public DbHealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/health/db")
    public ApiResponse<String> db() {
        // SELECT 1 실행해서 예외 없으면 OK
        Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
        if (one != null && one == 1) {
            return ApiResponse.ok("db-ok", null);
        }
        // 혹시 모를 경우 대비
        return ApiResponse.ok("db-unknown", null);
    }
}
