package com.example.community.common.exception;

import com.example.community.common.ApiResponse;
import com.example.community.common.ErrorCode;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Hidden
@RestControllerAdvice
@Order(1) // 가장 먼저
public class ValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handle(MethodArgumentNotValidException e) {
        String message = ErrorCode.BAD_REQUEST.getMessage();
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        if (errors != null && !errors.isEmpty()) {
            FieldError fe = errors.get(0);
            if (fe.getField() != null && fe.getDefaultMessage() != null) {
                message = fe.getField() + ": " + fe.getDefaultMessage();
            }
        }
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getStatus())
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, message));
    }
}
