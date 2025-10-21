package com.example.community.health;

// 공통 response
import com.example.community.common.ApiResponse;
// get mapping
import org.springframework.web.bind.annotation.GetMapping;
// rest controller
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("ok", null);

    }
}
