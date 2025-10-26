package com.example.community.common.exception;

import com.example.community.common.ApiResponse;
import com.example.community.common.ErrorCode;

public final class ErrorResponse {
    private ErrorResponse() {}
    public static ApiResponse<Void> of(ErrorCode ec, String messageOverride) {
        int status = ec.getStatus();
        String msg = (messageOverride != null && !messageOverride.isBlank())
                ? messageOverride : ec.getMessage();
        return ApiResponse.fail(status, msg);
    }
    public static ApiResponse<Void> of(ErrorCode ec) { return of(ec, null); }
}
