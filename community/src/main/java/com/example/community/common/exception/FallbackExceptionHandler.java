package com.example.community.common.exception;

import com.example.community.common.ApiResponse;
import com.example.community.common.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
@Order(3) // 마지막
public class FallbackExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(FallbackExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handle(Exception e) {
        // 여기서는 진짜 예상 못한 예외라 ERROR + 스택트레이스 출력
        log.error("[UnexpectedException] {}", e.getMessage(), e);

        return ResponseEntity.status(ErrorCode.SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.SERVER_ERROR));
    }
}
