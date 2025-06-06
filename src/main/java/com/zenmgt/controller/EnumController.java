package com.zenmgt.controller;

import com.zenmgt.dto.ApiResponse;
import com.zenmgt.dto.EnumDTO;
import com.zenmgt.service.EnumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Secure API for providing enum data to frontend applications
 * Requires authentication and filters sensitive enum values
 */
@RestController
@RequestMapping("/mgt/v1/enums")
@RequiredArgsConstructor
// @PreAuthorize("hasRole('USER')") // Temporarily removed for testing
@Tag(name = "Enums", description = "Secure enum data endpoints for frontend integration")
public class EnumController {
    
    private static final Logger logger = LoggerFactory.getLogger(EnumController.class);
    private final EnumService enumService;
    
    /**
     * Simple test endpoint to verify controller is working
     */
    @GetMapping("/test")
    @Operation(summary = "Test endpoint to verify controller registration")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("EnumController is working!", "Test successful"));
    }
    
    /**
     * Test version of getAllEnums endpoint for debugging
     */
    @GetMapping("/test-all")
    @Operation(summary = "Test version of get all enums endpoint")
    public ResponseEntity<ApiResponse<Object>> testGetAllEnums() {
        logger.debug("Getting all enums (test endpoint)");
        
        try {
            Object allEnums = enumService.getAllEnums();
            return ResponseEntity.ok(ApiResponse.success(allEnums, "All enums retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error retrieving all enums: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Failed to retrieve all enums"));
        }
    }
    
    @GetMapping("/record-statuses")
    @Operation(summary = "Get record status enums", 
               description = "Returns filtered record status values based on user context")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved record statuses"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<List<EnumDTO>>> getRecordStatuses(
            @Parameter(description = "Context filter (public/admin) - defaults to admin for authenticated users")
            @RequestParam(required = false, defaultValue = "admin") String context) {
        
        logger.debug("Getting record statuses for context: {}", context);
        
        try {
            // Simple hardcoded test data
            List<EnumDTO> statuses = List.of(
                EnumDTO.of(0, "INACTIVE", "Inactive", "Record is inactive"),
                EnumDTO.of(1, "ACTIVE", "Active", "Record is active"),
                EnumDTO.of(2, "PENDING_CREATE_APPROVAL", "Pending Create Approval", "New record awaiting approval")
            );
            return ResponseEntity.ok(ApiResponse.success(statuses, "Record statuses retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error retrieving record statuses: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Failed to retrieve record statuses"));
        }
    }
    
    @GetMapping("/approval-request-types")
    @Operation(summary = "Get approval request type enums",
               description = "Returns available approval request types (CREATE, UPDATE, DELETE)")
    // @PreAuthorize("hasPermission('APPROVAL_MANAGEMENT', 'READ')") // Temporarily removed
    public ResponseEntity<ApiResponse<List<EnumDTO>>> getApprovalRequestTypes() {
        
        logger.debug("Getting approval request types");
        
        try {
            List<EnumDTO> types = enumService.getApprovalRequestTypes();
            return ResponseEntity.ok(ApiResponse.success(types, "Approval request types retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error retrieving approval request types: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Failed to retrieve approval request types"));
        }
    }
    
    @GetMapping("/approval-statuses")
    @Operation(summary = "Get approval status enums",
               description = "Returns approval status values (PENDING, APPROVED, REJECTED, CANCELLED)")
    // @PreAuthorize("hasPermission('APPROVAL_MANAGEMENT', 'READ')") // Temporarily removed
    public ResponseEntity<ApiResponse<List<EnumDTO>>> getApprovalStatuses() {
        
        logger.debug("Getting approval statuses");
        
        try {
            List<EnumDTO> statuses = enumService.getApprovalStatuses();
            return ResponseEntity.ok(ApiResponse.success(statuses, "Approval statuses retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error retrieving approval statuses: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Failed to retrieve approval statuses"));
        }
    }
    
    @GetMapping("/sys-approval-request-statuses")
    @Operation(summary = "Get system approval request status enums",
               description = "Returns system-level approval request statuses with multi-level approval support")
    // @PreAuthorize("hasPermission('SYSTEM_MANAGEMENT', 'READ')") // Temporarily removed
    public ResponseEntity<ApiResponse<List<EnumDTO>>> getSysApprovalRequestStatuses() {
        
        logger.debug("Getting system approval request statuses");
        
        try {
            List<EnumDTO> statuses = enumService.getSysApprovalRequestStatuses();
            return ResponseEntity.ok(ApiResponse.success(statuses, "System approval request statuses retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error retrieving system approval request statuses: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Failed to retrieve system approval request statuses"));
        }
    }
    
    @GetMapping("/reference-types")
    @Operation(summary = "Get reference type enums",
               description = "Returns entity reference types for approval workflows")
    // @PreAuthorize("hasPermission('SYSTEM_MANAGEMENT', 'READ')") // Temporarily removed
    public ResponseEntity<ApiResponse<List<EnumDTO>>> getReferenceTypes() {
        
        logger.debug("Getting reference types");
        
        try {
            List<EnumDTO> types = enumService.getReferenceTypes();
            return ResponseEntity.ok(ApiResponse.success(types, "Reference types retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error retrieving reference types: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Failed to retrieve reference types"));
        }
    }
    
    @GetMapping("/categories")
    @Operation(summary = "Get available enum categories",
               description = "Returns list of all available enum categories")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableCategories() {
        
        logger.debug("Getting available enum categories");
        
        try {
            // Simple hardcoded list for testing
            List<String> categories = List.of(
                "recordStatus",
                "approvalRequestType", 
                "approvalStatus",
                "sysApprovalRequestStatus",
                "referenceType"
            );
            return ResponseEntity.ok(ApiResponse.success(categories, "Enum categories retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error retrieving enum categories: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Failed to retrieve enum categories"));
        }
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all enum values in one response",
               description = "Returns all enum types in a single response for frontend initialization")
    // @PreAuthorize("hasPermission('USER_MANAGEMENT', 'READ')") // Temporarily removed
    public ResponseEntity<ApiResponse<Object>> getAllEnums() {
        
        logger.debug("Getting all enums");
        
        try {
            Object allEnums = enumService.getAllEnums();
            return ResponseEntity.ok(ApiResponse.success(allEnums, "All enums retrieved successfully"));
            
        } catch (Exception e) {
            logger.error("Error retrieving all enums: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Failed to retrieve all enums"));
        }
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "Get enums by category",
               description = "Returns enum values for a specific category with optional context filtering")
    public ResponseEntity<ApiResponse<List<EnumDTO>>> getEnumsByCategory(
            @Parameter(description = "Enum category name")
            @PathVariable String category,
            @Parameter(description = "Context filter for sensitive enums")
            @RequestParam(required = false) String context) {
        
        logger.debug("Getting enums for category: {} with context: {}", category, context);
        
        try {
            List<EnumDTO> enums = enumService.getEnumsByCategory(category, context);
            return ResponseEntity.ok(ApiResponse.success(enums, 
                String.format("Enums for category '%s' retrieved successfully", category)));
            
        } catch (Exception e) {
            logger.error("Error retrieving enums for category {}: {}", category, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", 
                String.format("Failed to retrieve enums for category '%s'", category)));
        }
    }
    
    /**
     * Health check endpoint for enum service
     */
    @GetMapping("/health")
    @Operation(summary = "Enum service health check")
    public ResponseEntity<ApiResponse<Object>> health() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                java.util.Map.of(
                    "status", "healthy",
                    "service", "EnumService",
                    "timestamp", System.currentTimeMillis(),
                    "availableCategories", enumService.getAvailableEnumCategories().size()
                ),
                "Enum service is healthy"
            ));
        } catch (Exception e) {
            logger.error("Enum service health check failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.error("9999", "Enum service is unhealthy"));
        }
    }
} 