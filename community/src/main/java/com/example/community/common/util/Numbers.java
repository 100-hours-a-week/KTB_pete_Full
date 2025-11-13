package com.example.community.common.util;

import java.time.Instant;

public final class Numbers {
    private Numbers() {}

    // Long → 문자열, null이면 "0"
    public static String toStringOrZero(Long v) {
        if (v == null) {
            return "0";
        }
        return Long.toString(v);
    }

    // Long이면 long, null이면 0
    public static long longOrZero(Long v) {
        if (v == null) {
            return 0L;
        }
        return v.longValue();
    }

    // Instant이면 epochMilli, null이면 0
    public static long epochOrZero(Instant i) {
        if (i == null) {
            return 0L;
        }
        return i.toEpochMilli();
    }
}
