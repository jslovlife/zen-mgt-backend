package com.zenmgt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysApprovalRequestStatus {
    PENDING_CHECKER_L1(0),
    PENDING_CHECKER_L2(1),
    REJECTED_BY_CHECKER_L1(2),
    REJECTED_BY_CHECKER_L2(3),
    APPROVED(4);

    private final int value;
}
