//package com.example.community.common.exception;
//
//import com.example.community.common.ApiResponse;
//import com.example.community.common.ErrorCode;
//import io.swagger.v3.oas.annotations.Hidden;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@Hidden
//@RestControllerAdvice
//@Order(3) // 마지막
//public class FallbackExceptionHandler {
//
//    private static final Logger log = LoggerFactory.getLogger(FallbackExceptionHandler.class);
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Void>> handle(Exception e) {
//        // 여기서는 진짜 예상 못한 예외라 ERROR + 스택트레이스 출력
//        log.error("[UnexpectedException] {}", e.getMessage(), e);
//
//        return ResponseEntity.status(ErrorCode.SERVER_ERROR.getStatus())
//                .body(ErrorResponse.of(ErrorCode.SERVER_ERROR));
//    }
//}

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

        // 🔥 1) 콘솔에 전체 스택트레이스 남기기
        log.error("[UnexpectedException] type={}, message={}",
                e.getClass().getName(), e.getMessage(), e);

        // 🔥 2) 개발 단계니까, 어떤 예외인지 응답 message에도 같이 실어 보자
        String debugMessage = "[" + e.getClass().getSimpleName() + "] "
                + ErrorCode.SERVER_ERROR.getMessage();

        return ResponseEntity
                .status(ErrorCode.SERVER_ERROR.getStatus())
                // 원래는 ErrorResponse.of(...)였는데, 잠깐 디버깅용으로 직접 메시지 넣자
                .body(ApiResponse.fail(
                        ErrorCode.SERVER_ERROR.getStatus(),
                        debugMessage
                ));
    }
}

