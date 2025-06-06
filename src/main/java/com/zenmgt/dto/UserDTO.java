package com.zenmgt.dto;

import com.zenmgt.enums.RecordStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class UserDTO {
    // Auth User fields
    private Long id;
    private String userCode;
    
    // Replace isActive with recordStatus
    private Integer recordStatus;
    private String recordStatusDescription;
    
    private String sessionKey;
    private Long activeVersion;
    private LocalDateTime lastLoginAt;
    
    // Auth User Detail fields
    private String username;
    private String email;
    private String status;
    private Long sessionValidity; // Session validity in milliseconds
    
    // Approval fields
    private Integer approvalStatus;
    private String approvalStatusDescription;
    private Long approvalRequestId;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private String createdByUsername;
    private String updatedByUsername;

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
} 