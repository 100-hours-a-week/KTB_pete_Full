package com.example.community.common.sort;

public enum SortDir {
    ASC, DESC;

    public static SortDir from(String raw) {
        if (raw == null) {
            return DESC;
        }
        String v = raw.trim();
        if (v.isEmpty()) {
            return DESC;
        }
        if ("asc".equalsIgnoreCase(v)) {
            return ASC;
        }
        if ("desc".equalsIgnoreCase(v)) {
            return DESC;
        }
        return DESC;
    }
}
