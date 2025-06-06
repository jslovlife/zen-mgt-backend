package com.zenmgt.dto;

import com.zenmgt.enums.ReferenceType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
public class ApprovalRequestDTO {
    private Long id;
    private Integer requestType;
    private String requestTypeDescription;
    private Integer requestStatus;
    private String requestStatusDescription;
    private ReferenceType referenceType;
    private String referenceTypeDescription;
    private Long referenceId;
    private Long referenceVersionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String createdByUsername;
    private Long updatedBy;
    private String updatedByUsername;

    // Helper methods to get descriptions
    public String getRequestTypeDescription() {
        if (requestType == null) return null;
        switch (requestType) {
            case 1: return "CREATE";
            case 2: return "UPDATE";
            case 3: return "DELETE";
            case 4: return "READ";
            default: return "UNKNOWN";
        }
    }

    public String getRequestStatusDescription() {
        if (requestStatus == null) return null;
        switch (requestStatus) {
            case 0: return "PENDING";
            case 1: return "PENDING_CHECKER";
            case 2: return "PENDING_CHECKER1";
            case 3: return "PENDING_CHECKER2";
            case 4: return "APPROVED";
            case 5: return "REJECTED";
            default: return "UNKNOWN";
        }
    }

    public String getReferenceTypeDescription() {
        if (referenceType == null) return null;
        switch (referenceType) {
            case AUTH_USER: return "User Management";
            case AUTH_USER_GROUP: return "User Group Management";
            case SYSTEM_PARAM: return "System Parameters";
            default: return "UNKNOWN";
        }
    }
} 