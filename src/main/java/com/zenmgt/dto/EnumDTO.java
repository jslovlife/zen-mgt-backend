package com.zenmgt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized DTO for enum values
 * Provides consistent structure across all enum endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumDTO {
    
    @JsonProperty("code")
    private Object code;  // Can be int, string, etc.
    
    @JsonProperty("name")
    private String name;  // Enum constant name (e.g., "ACTIVE")
    
    @JsonProperty("display")
    private String display;  // Human-readable display name
    
    @JsonProperty("description")
    private String description;  // Detailed description
    
    @JsonProperty("category")
    private String category;  // Optional category grouping
    
    @JsonProperty("sortOrder")
    private Integer sortOrder;  // For frontend ordering
    
    @JsonProperty("isDefault")
    private Boolean isDefault;  // Mark default values
    
    @JsonProperty("metadata")
    private Object metadata;  // Additional properties (flexible)
    
    // Helper constructors for common use cases
    public static EnumDTO of(int code, String name, String display) {
        return EnumDTO.builder()
            .code(code)
            .name(name)
            .display(display)
            .build();
    }
    
    public static EnumDTO of(String code, String name, String display) {
        return EnumDTO.builder()
            .code(code)
            .name(name)
            .display(display)
            .build();
    }
    
    public static EnumDTO of(int code, String name, String display, String description) {
        return EnumDTO.builder()
            .code(code)
            .name(name)
            .display(display)
            .description(description)
            .build();
    }
    
    public static EnumDTO of(String code, String name, String display, String description) {
        return EnumDTO.builder()
            .code(code)
            .name(name)
            .display(display)
            .description(description)
            .build();
    }
} 