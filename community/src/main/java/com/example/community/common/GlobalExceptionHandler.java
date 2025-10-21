package com.example.community.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // HTTP 상태코드 그대로 사용, 본문 code도 int
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode ec = e.getErrorCode();
        return ResponseEntity
                .status(ec.getStatus()) // ← .value() 제거 (getStatus()가 int)
                .body(ApiResponse.fail(ec.getStatus(), ec.getMessage()));
    }

    // 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<org.springframework.validation.FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        String message = ErrorCode.BAD_REQUEST.getMessage();
        if (fieldErrors != null && fieldErrors.size() > 0) {
            org.springframework.validation.FieldError fe = fieldErrors.get(0);
            if (fe != null && fe.getField() != null && fe.getDefaultMessage() != null) {
                message = fe.getField() + ": " + fe.getDefaultMessage();
            }
        }

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus()) // ← .value() 제거
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getStatus(), message));
    }

    // 기타 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleEtc(Exception e) {
        //e.printStackTrace(); //디버깅 용
        return ResponseEntity
                .status(ErrorCode.SERVER_ERROR.getStatus()) // ← .value() 제거
                .body(ApiResponse.fail(ErrorCode.SERVER_ERROR.getStatus(), ErrorCode.SERVER_ERROR.getMessage()));
    }
}
