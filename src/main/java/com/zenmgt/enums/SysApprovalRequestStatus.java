package com.zenmgt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysApprovalRequestStatus {
    PENDING_CHECKER_L1(0),
    PENDING_CHECKER_L2(1),
    PENDING_CHECKER_L3(2),
    REJECTED_BY_CHECKER_L1(3),
    REJECTED_BY_CHECKER_L2(4),
    REJECTED_BY_CHECKER_L3(5),
    APPROVED(6);

    private final int value;
}
