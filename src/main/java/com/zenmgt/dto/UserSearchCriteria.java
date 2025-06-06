package com.zenmgt.dto;

import com.zenmgt.enums.RecordStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Flexible search criteria for user queries
 * Supports multiple filter types and combinations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCriteria {
    
    // Pagination fields
    @Builder.Default
    private Integer page = 1;
    @Builder.Default
    private Integer pageSize = 20;
    @Builder.Default
    private String sortBy = "createdAt";
    @Builder.Default
    private String sortDirection = "desc";
    
    // Text-based searches (supports LIKE patterns)
    private String userCode;
    private String username;
    private String email;
    
    // Global text search (searches across all text fields)
    private String globalSearch;
    
    // Status-based filters
    private String recordStatus;
    private List<RecordStatus> recordStatuses;
    
    // Date range filters
    private LocalDateTime createdDateFrom;
    private LocalDateTime createdDateTo;
    private LocalDateTime updatedDateFrom;
    private LocalDateTime updatedDateTo;
    private LocalDateTime lastLoginFrom;
    private LocalDateTime lastLoginTo;
    
    // Additional filters
    private Long createdBy;
    private Long updatedBy;
    
    // Search options
    @Builder.Default
    private boolean exactMatch = false; // If true, use = instead of LIKE
    @Builder.Default
    private boolean caseSensitive = false;
    
    /**
     * Check if any search criteria is provided
     */
    public boolean hasAnyCriteria() {
        return username != null || email != null  ||
               globalSearch != null || recordStatus != null || recordStatuses != null ||
               createdDateFrom != null || createdDateTo != null ||
               updatedDateFrom != null || updatedDateTo != null ||
               lastLoginFrom != null || lastLoginTo != null ||
               createdBy != null || updatedBy != null;
    }
    
    /**
     * Get LIKE pattern for text searches
     */
    public String getLikePattern(String value) {
        if (value == null) return null;
        return exactMatch ? value : "%" + value + "%";
    }
    
    /**
     * Apply case sensitivity to text value
     */
    public String applyCaseSensitivity(String value) {
        if (value == null) return null;
        return caseSensitive ? value : value.toLowerCase();
    }
} 