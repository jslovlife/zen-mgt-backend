package com.zenmgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Specialized pagination response for Groups module
 * JSON output: { "groups": [...], "total": 100, "page": 1, "totalPages": 5 }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPagedResponseDTO<T> {
    @JsonProperty("groups")
    private List<T> data;
    private long total;
    private int page;
    private int totalPages;
    private int pageSize;
    
    public static <T> GroupPagedResponseDTO<T> from(PagedResponseDTO<T> genericPage) {
        return GroupPagedResponseDTO.<T>builder()
                .data(genericPage.getData())
                .total(genericPage.getTotal())
                .page(genericPage.getPage())
                .totalPages(genericPage.getTotalPages())
                .pageSize(genericPage.getPageSize())
                .build();
    }
} 