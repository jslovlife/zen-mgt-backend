package com.zenmgt.service;

import com.zenmgt.dto.EnumDTO;
import com.zenmgt.enums.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for providing secure enum data to frontend
 * Filters sensitive values and provides cached responses
 */
@Service
@RequiredArgsConstructor
public class EnumServiceImpl implements EnumService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnumServiceImpl.class);
    
    // Define which enum values are safe for frontend consumption
    private static final Set<RecordStatus> PUBLIC_RECORD_STATUSES = Set.of(
        RecordStatus.INACTIVE,
        RecordStatus.ACTIVE
    );
    
    private static final Set<RecordStatus> ADMIN_RECORD_STATUSES = Set.of(
        RecordStatus.INACTIVE,
        RecordStatus.ACTIVE,
        RecordStatus.PENDING_CREATE_APPROVAL,
        RecordStatus.PENDING_AMENDMENT_APPROVAL,
        RecordStatus.PENDING_DELETE_APPROVAL
        // Exclude DELETED for security
    );
    
    private static final Set<ApprovalRequestType> PUBLIC_APPROVAL_TYPES = Set.of(
        ApprovalRequestType.CREATE,
        ApprovalRequestType.UPDATE,
        ApprovalRequestType.DELETE
    );
    
    @Override
    @Cacheable("enumCache")
    public List<EnumDTO> getRecordStatuses() {
        return getRecordStatuses("admin"); // Default to admin context
    }
    
    @Override
    @Cacheable(value = "enumCache", key = "'recordStatus_' + #context")
    public List<EnumDTO> getRecordStatuses(String context) {
        logger.debug("Getting record statuses for context: {}", context);
        
        Set<RecordStatus> allowedStatuses;
        switch (context != null ? context.toLowerCase() : "public") {
            case "admin":
            case "system":
                allowedStatuses = ADMIN_RECORD_STATUSES;
                break;
            case "public":
            case "user":
            default:
                allowedStatuses = PUBLIC_RECORD_STATUSES;
                break;
        }
        
        return allowedStatuses.stream()
            .map(status -> EnumDTO.builder()
                .code(status.getCode())
                .name(status.name())
                .display(status.getDescription())
                .description(getRecordStatusDescription(status))
                .category("recordStatus")
                .sortOrder(status.getCode())
                .isDefault(status == RecordStatus.INACTIVE)
                .metadata(Map.of(
                    "toggleable", status.isToggleable(),
                    "isPending", status.isPendingApproval(),
                    "isActive", status.isEffectivelyActive()
                ))
                .build())
            .sorted(Comparator.comparing(dto -> (Integer) dto.getCode()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable("enumCache")
    public List<EnumDTO> getApprovalRequestTypes() {
        logger.debug("Getting approval request types");
        
        return PUBLIC_APPROVAL_TYPES.stream()
            .map(type -> EnumDTO.builder()
                .code(type.getCode())
                .name(type.name())
                .display(type.getDescription())
                .description(getApprovalTypeDescription(type))
                .category("approvalRequestType")
                .sortOrder(type.getCode())
                .metadata(Map.of(
                    "pendingStatus", type.getPendingStatus().getCode(),
                    "approvedStatus", type.getApprovedStatus().getCode()
                ))
                .build())
            .sorted(Comparator.comparing(dto -> (Integer) dto.getCode()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable("enumCache")
    public List<EnumDTO> getApprovalStatuses() {
        logger.debug("Getting approval statuses");
        
        return Arrays.stream(ApprovalStatus.values())
            .map(status -> EnumDTO.builder()
                .code(status.getValue())
                .name(status.name())
                .display(formatDisplayName(status.name()))
                .description(getApprovalStatusDescription(status))
                .category("approvalStatus")
                .sortOrder(status.ordinal())
                .metadata(Map.of(
                    "isFinal", status == ApprovalStatus.APPROVED || status == ApprovalStatus.REJECTED
                ))
                .build())
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable("enumCache")
    public List<EnumDTO> getSysApprovalRequestStatuses() {
        logger.debug("Getting system approval request statuses");
        
        return Arrays.stream(SysApprovalRequestStatus.values())
            .map(status -> EnumDTO.builder()
                .code(status.getValue())
                .name(status.name())
                .display(formatDisplayName(status.name()))
                .description(getSysApprovalStatusDescription(status))
                .category("sysApprovalRequestStatus")
                .sortOrder(status.getValue())
                .metadata(Map.of(
                    "isPending", status.getValue() <= 2,
                    "isRejected", status.getValue() >= 3 && status.getValue() <= 5,
                    "isApproved", status == SysApprovalRequestStatus.APPROVED
                ))
                .build())
            .sorted(Comparator.comparing(dto -> (Integer) dto.getCode()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable("enumCache")
    public List<EnumDTO> getReferenceTypes() {
        logger.debug("Getting reference types");
        
        return Arrays.stream(ReferenceType.values())
            .map(type -> EnumDTO.builder()
                .code(type.getValue())
                .name(type.name())
                .display(formatDisplayName(type.name()))
                .description(getReferenceTypeDescription(type))
                .category("referenceType")
                .sortOrder(type.getValue())
                .build())
            .sorted(Comparator.comparing(dto -> (Integer) dto.getCode()))
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable("enumCache")
    public List<String> getAvailableEnumCategories() {
        return List.of(
            "recordStatus",
            "approvalRequestType", 
            "approvalStatus",
            "sysApprovalRequestStatus",
            "referenceType"
        );
    }
    
    @Override
    @Cacheable("enumCache")
    public Object getAllEnums() {
        logger.debug("Getting all enums");
        
        Map<String, Object> allEnums = new HashMap<>();
        allEnums.put("recordStatuses", getRecordStatuses("admin"));
        allEnums.put("approvalRequestTypes", getApprovalRequestTypes());
        allEnums.put("approvalStatuses", getApprovalStatuses());
        allEnums.put("sysApprovalRequestStatuses", getSysApprovalRequestStatuses());
        allEnums.put("referenceTypes", getReferenceTypes());
        allEnums.put("categories", getAvailableEnumCategories());
        allEnums.put("lastUpdated", System.currentTimeMillis());
        
        return allEnums;
    }
    
    @Override
    @Cacheable(value = "enumCache", key = "'category_' + #category + '_' + #context")
    public List<EnumDTO> getEnumsByCategory(String category, String context) {
        logger.debug("Getting enums by category: {} with context: {}", category, context);
        
        return switch (category != null ? category.toLowerCase() : "") {
            case "recordstatus" -> getRecordStatuses(context);
            case "approvalrequesttype" -> getApprovalRequestTypes();
            case "approvalstatus" -> getApprovalStatuses();
            case "sysapprovalrequeststatus" -> getSysApprovalRequestStatuses();
            case "referencetype" -> getReferenceTypes();
            default -> Collections.emptyList();
        };
    }
    
    // Helper methods for descriptions
    private String getRecordStatusDescription(RecordStatus status) {
        return switch (status) {
            case INACTIVE -> "Record is inactive and not visible to users";
            case ACTIVE -> "Record is active and available for use";
            case PENDING_CREATE_APPROVAL -> "New record awaiting approval";
            case PENDING_AMENDMENT_APPROVAL -> "Record changes awaiting approval";
            case PENDING_DELETE_APPROVAL -> "Record deletion awaiting approval";
            case DELETED -> "Record has been permanently deleted";
        };
    }
    
    private String getApprovalTypeDescription(ApprovalRequestType type) {
        return switch (type) {
            case CREATE -> "Request to create a new record";
            case UPDATE -> "Request to modify an existing record";
            case DELETE -> "Request to delete an existing record";
        };
    }
    
    private String getApprovalStatusDescription(ApprovalStatus status) {
        return switch (status) {
            case PENDING -> "Approval request is pending review";
            case APPROVED -> "Request has been approved";
            case REJECTED -> "Request has been rejected";
            case CANCELLED -> "Request has been cancelled";
        };
    }
    
    private String getSysApprovalStatusDescription(SysApprovalRequestStatus status) {
        return switch (status) {
            case PENDING_CHECKER_L1 -> "Pending Level 1 checker approval";
            case PENDING_CHECKER_L2 -> "Pending Level 2 checker approval";
            case PENDING_CHECKER_L3 -> "Pending Level 3 checker approval";
            case REJECTED_BY_CHECKER_L1 -> "Rejected by Level 1 checker";
            case REJECTED_BY_CHECKER_L2 -> "Rejected by Level 2 checker";
            case REJECTED_BY_CHECKER_L3 -> "Rejected by Level 3 checker";
            case APPROVED -> "Request has been fully approved";
        };
    }
    
    private String getReferenceTypeDescription(ReferenceType type) {
        return switch (type) {
            case AUTH_USER -> "User management operations";
            case AUTH_USER_GROUP -> "User group management operations";
            case SYSTEM_PARAM -> "System parameter management operations";
        };
    }
    
    private String formatDisplayName(String enumName) {
        return Arrays.stream(enumName.split("_"))
            .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
} 