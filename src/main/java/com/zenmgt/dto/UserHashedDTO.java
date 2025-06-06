package com.zenmgt.dto;

import com.zenmgt.enums.RecordStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for AuthUser that uses hashed IDs for enhanced security.
 * Frontend should only work with hashed IDs, never raw database IDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHashedDTO {
    
    // Hashed IDs for security
    private String hashedUserId;           // Hashed version of auth_user.id
    private String hashedUserGroupId;      // Hashed version of user_group.id
    private String hashedCreatedBy;        // Hashed version of created_by
    private String hashedUpdatedBy;        // Hashed version of updated_by
    
    // User identification and authentication
    private String username;
    private String email;
    
    // System fields - Replace isActive with recordStatus
    private Integer recordStatus;
    private String recordStatusDescription;
    private String sessionKey;
    private LocalDateTime lastLoginAt;
    private Long sessionValidity;
    
    // Approval fields
    private Integer approvalStatus;
    private String approvalStatusDescription;
    private Long approvalRequestId;
    
    // Audit fields (for display purposes only)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for creation/update operations
    private String password;               // For user creation/password updates
    private String confirmPassword;        // For password confirmation

    // Helper methods for RecordStatus
    public RecordStatus getRecordStatusEnum() {
        return recordStatus != null ? RecordStatus.fromCode(recordStatus) : RecordStatus.INACTIVE;
    }

    public void setRecordStatusEnum(RecordStatus status) {
        this.recordStatus = status.getCode();
        this.recordStatusDescription = status.getDescription();
    }

    public boolean canToggleStatus() {
        RecordStatus status = getRecordStatusEnum();
        return status.isToggleable();
    }

    public boolean isActive() {
        return recordStatus != null && recordStatus == 1;
    }

    public boolean isDeleted() {
        return recordStatus != null && recordStatus == 5;
    }

    public boolean isPendingApproval() {
        return recordStatus != null && 
               (recordStatus == 2 || recordStatus == 3 || recordStatus == 4);
    }
    
    /**
     * Create a DTO for user creation (no existing IDs)
     */
    public static UserHashedDTO forCreation(String username, String email, String firstName, String lastName) {
        return UserHashedDTO.builder()
            .username(username)
            .email(email)
            .recordStatus(RecordStatus.PENDING_CREATE_APPROVAL.getCode())
            .build();
    }
    
    /**
     * Create a DTO for user update (with existing hashed user ID)
     */
    public static UserHashedDTO forUpdate(String hashedUserId) {
        return UserHashedDTO.builder()
            .hashedUserId(hashedUserId)
            .build();
    }
} 