package com.zenmgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic pagination response wrapper for API responses
 * Can be used for any module with configurable data field name:
 * 
 * For Users: { "users": [...], "total": 1250, "page": 1, "totalPages": 50 }
 * For Groups: { "groups": [...], "total": 100, "page": 1, "totalPages": 5 }
 * For Resources: { "resources": [...], "total": 500, "page": 1, "totalPages": 25 }
 * 
 * @param <T> The type of data being paginated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDTO<T> {
    
    private List<T> data;         // Generic data array (will be serialized with custom name)
    private long total;           // Total number of records across all pages
    private int page;             // Current page number (1-based)
    private int totalPages;       // Total number of pages
    private int pageSize;         // Number of items per page
    
    /**
     * Create a PagedResponseDTO from Spring Data Page object
     * @param springPage The Spring Data Page object
     * @param <T> The type of data
     * @return PagedResponseDTO with proper pagination info
     */
    public static <T> PagedResponseDTO<T> from(Page<T> springPage) {
        return PagedResponseDTO.<T>builder()
                .data(springPage.getContent())
                .total(springPage.getTotalElements())
                .page(springPage.getNumber() + 1) // Convert 0-based to 1-based
                .totalPages(springPage.getTotalPages())
                .pageSize(springPage.getSize())
                .build();
    }
    
    /**
     * Create a PagedResponseDTO from a list (when pagination is not used)
     * @param data The data list
     * @param <T> The type of data
     * @return PagedResponseDTO representing all data as a single page
     */
    public static <T> PagedResponseDTO<T> from(List<T> data) {
        return PagedResponseDTO.<T>builder()
                .data(data)
                .total(data.size())
                .page(1)
                .totalPages(1)
                .pageSize(data.size())
                .build();
    }
} 