package com.zenmgt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApprovalRequestType {
    CREATE(1, "Create"),
    UPDATE(2, "Update"), 
    DELETE(3, "Delete");

    private final int code;
    private final String description;

    public static ApprovalRequestType fromCode(int code) {
        for (ApprovalRequestType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid request type code: " + code);
    }

    /**
     * Get the pending approval status for this request type
     */
    public RecordStatus getPendingStatus() {
        return switch (this) {
            case CREATE -> RecordStatus.PENDING_CREATE_APPROVAL;
            case UPDATE -> RecordStatus.PENDING_AMENDMENT_APPROVAL;
            case DELETE -> RecordStatus.PENDING_DELETE_APPROVAL;
        };
    }

    /**
     * Get the target status when request is approved
     */
    public RecordStatus getApprovedStatus() {
        return switch (this) {
            case CREATE -> RecordStatus.ACTIVE;
            case UPDATE -> RecordStatus.ACTIVE;
            case DELETE -> RecordStatus.DELETED;
        };
    }
} 