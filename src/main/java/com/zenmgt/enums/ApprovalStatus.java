package com.zenmgt.enums;

import lombok.Getter;

@Getter
public enum ApprovalStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED");

    private final String value;

    ApprovalStatus(String value) {
        this.value = value;
    }
} 