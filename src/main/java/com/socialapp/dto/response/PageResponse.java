package com.socialapp.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper chung cho mọi API có phân trang.
 * Client nhận được: danh sách items + thông tin trang.
 */
public record PageResponse<T>(
        List<T> content,
        int page,           // trang hiện tại (bắt đầu từ 0)
        int size,           // số item mỗi trang
        long totalElements, // tổng số item
        int totalPages,     // tổng số trang
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}