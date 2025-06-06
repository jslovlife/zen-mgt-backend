package com.zenmgt.model;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private String status = "ERROR";
    private Integer errorCode;          // Numeric error code (e.g., 5001, 9001)
    private String errorType;           // Error category (e.g., "SYSTEM_ERROR", "VALIDATION_ERROR")
    private String message;             // Human-readable error description
    private LocalDateTime timestamp;
    private String requestId;           // For tracing
    
    // Performance metadata
    private Long queryTimeMs;
    private String performanceHint;     // Suggest optimizations
    
    // Security considerations
    private boolean sensitiveDataMasked = true; // Never expose internal IDs
    
    // Additional context for debugging (optional)
    private Map<String, Object> context; // Field-specific error details
}
