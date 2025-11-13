package com.example.community.common.sort;

public enum CommentSortBy {
    CREATED_AT;

    public static CommentSortBy from(String raw) {
        return CREATED_AT;
    }
}
