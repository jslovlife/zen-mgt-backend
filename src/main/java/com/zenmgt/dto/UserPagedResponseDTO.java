package com.zenmgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Specialized pagination response for Users module
 * JSON output: { "users": [...], "total": 1250, "page": 1, "totalPages": 50 }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPagedResponseDTO<T> {
    private List<T> users;
    private long total;
    private int page;
    private int totalPages;
    private int pageSize;
    
    public static <T> UserPagedResponseDTO<T> from(PagedResponseDTO<T> genericPage) {
        return UserPagedResponseDTO.<T>builder()
                .users(genericPage.getData())
                .total(genericPage.getTotal())
                .page(genericPage.getPage())
                .totalPages(genericPage.getTotalPages())
                .pageSize(genericPage.getPageSize())
                .build();
    }
} 