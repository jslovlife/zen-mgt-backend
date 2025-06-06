package com.zenmgt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordStatus {
    INACTIVE(0, "Inactive"),
    ACTIVE(1, "Active"),
    PENDING_CREATE_APPROVAL(2, "Pending Create Approval"),
    PENDING_AMENDMENT_APPROVAL(3, "Pending Amendment Approval"),
    PENDING_DELETE_APPROVAL(4, "Pending Delete Approval"),
    DELETED(5, "Deleted");

    private final int code;
    private final String description;

    /**
     * Get RecordStatus by code
     */
    public static RecordStatus fromCode(int code) {
        for (RecordStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid record status code: " + code);
    }

    /**
     * Check if status allows activation/deactivation
     */
    public boolean isToggleable() {
        return this == ACTIVE || this == INACTIVE;
    }

    /**
     * Check if record is in pending approval state
     */
    public boolean isPendingApproval() {
        return this == PENDING_CREATE_APPROVAL || 
               this == PENDING_AMENDMENT_APPROVAL || 
               this == PENDING_DELETE_APPROVAL;
    }

    /**
     * Check if record is effectively active
     */
    public boolean isEffectivelyActive() {
        return this == ACTIVE;
    }

    /**
     * Check if record is soft deleted
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
} 