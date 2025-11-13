package com.example.community.common.util;

public final class Strings {
    private Strings() {}

    // null이면  ""
    public static String nullToEmpty(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    //null 또는 공백문자면 null, 그 외엔 trim된 문자열
    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    // 전부 공백이면 null, 아니면 원본 trim X
    public static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return s;
            }
        }
        return null;
    }

    /** (옵션) 표시용: null이면 "unknown" */
    public static String orUnknown(Object v) {
        if (v == null) {
            return "unknown";
        }
        return v.toString();
    }
}
