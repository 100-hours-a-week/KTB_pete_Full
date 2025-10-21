package com.example.community.dto.post;

public class PageMeta {
    public int page;
    public int size;
    public long totalElements;
    public int totalPages;

    public PageMeta(int page, int size, long totalElements, int totalPages) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
