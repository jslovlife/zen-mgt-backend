package com.zenmgt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestType {
    CREATE(1),
    UPDATE(2),
    DELETE(3);

    private final int value;

    public static RequestType fromValue(int value) {
        for (RequestType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid request type value: " + value);
    }
} 