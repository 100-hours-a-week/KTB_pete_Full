package com.example.community.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "isSuccess", "code", "message", "result" })
public class ApiResponse<T> {

    // 내부 필드명은 success로 유지하되 JSON에는 노출하지 않음
    @JsonIgnore
    private boolean success;

    // 설계대로 code는 숫자로 유지
    private int code;

    private String message;
    private T result;

    public ApiResponse() {}

    public ApiResponse(boolean success, int code, String message, T result) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    // JSON 키를 "isSuccess"로 강제
    @JsonGetter("isSuccess")
    public boolean isSuccess() { return success; }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getResult() { return result; }

    // 편의 메서드
    public static <T> ApiResponse<T> ok(String message, T result) {
        return new ApiResponse<>(true, 200, message, result);
    }

    public static <T> ApiResponse<T> ok(T result) {
        return new ApiResponse<>(true, 200, "OK", result);
    }

    public static <T> ApiResponse<T> fail(int httpStatus, String message) {
        return new ApiResponse<>(false, httpStatus, message, null);
    }
}
