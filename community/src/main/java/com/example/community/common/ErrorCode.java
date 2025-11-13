package com.example.community.common;

public enum ErrorCode {
    // 400 계열
    BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다.", 400),
    REQUEST_INCORRECT("REQUEST_INCORRECT", "요청 값이 올바르지 않습니다.", 400),
    TOKEN_FORMAT_INCORRECT("TOKEN_FORMAT_INCORRECT", "토큰 형식이 올바르지 않습니다.", 401),

    // 401/403
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", 401),
    LOGIN_PASSWORD_WRONG("LOGIN_PASSWORD_WRONG", "비밀번호가 일치하지 않습니다.", 401),
    FORBIDDEN("FORBIDDEN", "권한이 없습니다.", 403),

    // 404/409
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다.", 404),
    POST_NOT_FOUND("POST_NOT_FOUND", "게시글을 찾을 수 없습니다.", 404),
    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.", 404),
    EMAIL_ALREADY_EXIST("EMAIL_ALREADY_EXIST", "이미 사용 중인 이메일입니다.", 409),

    // 500
    SERVER_ERROR("SERVER_ERROR", "서버 오류가 발생했습니다.", 500);

    private final String code;
    private final String message;
    private final int status;

    ErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
}
