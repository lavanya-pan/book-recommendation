package com.sds.bookrecommendation.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * One page of results in a stable JSON shape: the items plus paging information.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /** Builds a PageResponse from a Spring Data page. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}