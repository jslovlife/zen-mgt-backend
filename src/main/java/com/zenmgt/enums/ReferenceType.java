package com.zenmgt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReferenceType {
    AUTH_USER(100),
    AUTH_USER_GROUP(200),
    SYSTEM_PARAM(300);

    private final int value;

    public static ReferenceType fromValue(int value) {
        for (ReferenceType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid reference type value: " + value);
    }
} 