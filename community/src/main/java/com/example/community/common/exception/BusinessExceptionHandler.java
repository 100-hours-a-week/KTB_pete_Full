package com.example.community.common.exception;

import com.example.community.common.ApiResponse;
import com.example.community.common.BusinessException;
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
@Order(2)
public class BusinessExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BusinessExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handle(BusinessException e) {
        ErrorCode ec = e.getErrorCode();

        if (ec != null) {
            // 4xx 계열이 대부분이라 WARN 정도로 남김 (필요하면 ERROR로 바꿔도 됨)
            log.warn(
                    "[BusinessException] code={}, status={}, message={}",
                    ec.name(), ec.getStatus(), ec.getMessage(), e
            );
        } else {
            log.warn("[BusinessException] (no ErrorCode) message={}", e.getMessage(), e);
        }

        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec));
    }
}
