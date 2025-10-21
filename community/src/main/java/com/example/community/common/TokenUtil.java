package com.example.community.common; // 공통 패키지

// jwt 토큰 형식 대신용
public class TokenUtil {

    // 헤더에서 userId 추출 및 오류 던지기
    public static Long resolveUserId(String authorizationHeader) {
        // 토큰 절차
        if (authorizationHeader == null) {
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }

        String token = authorizationHeader.substring("Bearer ".length());
        if (token == null) {
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }
        token = token.trim(); // 앞뒤 공백 제거

        // 예시 토큰
        String prefix = "dummy-";
        if (!token.startsWith(prefix)) {
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }

        // 토큰에서 userId 획득
        String idStr = token.substring(prefix.length());
        if (idStr == null) {
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }
        idStr = idStr.trim();

        // 숫자 변환 시도
        try {
            Long userId = Long.parseLong(idStr);
            return userId;
        } catch (NumberFormatException nfe) {
            // 숫자로 변환 실패 시 형식 오류
            throw new BusinessException(ErrorCode.TOKEN_FORMAT_INCORRECT);
        }
    }


    public static String issueDummyToken(Long userId) {
        if (userId == null) { // 널 방어
            return "dummy-0"; // 0을 기본값으로
        }
        return "dummy-" + userId; // 간단 연결
    }
}
