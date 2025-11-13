package com.example.community.common.util;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public final class Times {
    private Times() {}

    // UTC or null이면 unknown
    public static String toIsoOrUnknown(Instant i) {
        if (i == null) {
            return "unknown";
        }
        return DateTimeFormatter.ISO_INSTANT.format(i);
    }
}
