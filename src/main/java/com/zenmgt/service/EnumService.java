package com.zenmgt.service;

import com.zenmgt.dto.EnumDTO;
import java.util.List;

/**
 * Service for providing secure enum data to frontend
 * Handles filtering, caching, and security for enum values
 */
public interface EnumService {
    
    /**
     * Get all record status values (filtered for frontend)
     */
    List<EnumDTO> getRecordStatuses();
    
    /**
     * Get record status values for specific context
     */
    List<EnumDTO> getRecordStatuses(String context);
    
    /**
     * Get approval request types
     */
    List<EnumDTO> getApprovalRequestTypes();
    
    /**
     * Get approval statuses
     */
    List<EnumDTO> getApprovalStatuses();
    
    /**
     * Get approval request statuses (system-level)
     */
    List<EnumDTO> getSysApprovalRequestStatuses();
    
    /**
     * Get reference types
     */
    List<EnumDTO> getReferenceTypes();
    
    /**
     * Get all enum categories available
     */
    List<String> getAvailableEnumCategories();
    
    /**
     * Get multiple enum types in one response
     */
    Object getAllEnums();
    
    /**
     * Get enums by category with optional filtering
     */
    List<EnumDTO> getEnumsByCategory(String category, String context);
} 