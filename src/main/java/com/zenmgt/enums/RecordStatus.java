package com.zenmgt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordStatus {
    ACTIVE(1),
    INACTIVE(0),
    DELETED(3);

    private final int value;
} 