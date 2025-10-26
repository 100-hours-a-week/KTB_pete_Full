package com.example.community.common.exception;

import com.example.community.common.ApiResponse;
import com.example.community.common.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
@Order(3) // 마지막
public class FallbackExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handle(Exception e) {
        // 필요 시 로그: log.error("Unexpected error", e);
        return ResponseEntity.status(ErrorCode.SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.SERVER_ERROR));
    }
}
