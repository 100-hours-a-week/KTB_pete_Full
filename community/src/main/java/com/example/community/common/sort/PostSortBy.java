package com.example.community.common.sort;

public enum PostSortBy {
    CREATED_AT, TITLE, VIEWS, LIKES;

    public static PostSortBy from(String raw) {
        if (raw == null) {
            return CREATED_AT;
        }
        String v = raw.trim();
        if (v.isEmpty()) {
            return CREATED_AT;
        }
        if ("createdAt".equalsIgnoreCase(v)) {
            return CREATED_AT;
        }
        if ("title".equalsIgnoreCase(v)) {
            return TITLE;
        }
        if ("views".equalsIgnoreCase(v)) {
            return VIEWS;
        }
        if ("likes".equalsIgnoreCase(v)) {
            return LIKES;
        }
        return CREATED_AT;
    }
}
