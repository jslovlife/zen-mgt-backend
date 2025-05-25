package com.zenmgt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysApprovalParamType {
    STRING(0),
    JSON(1);

    private final int value;
}
