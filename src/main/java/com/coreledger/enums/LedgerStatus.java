package com.coreledger.enums;

import lombok.Getter;

/**
 * Ledger Status Enum
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum LedgerStatus {
    IN_PROGRESS("进行中"),
    PARTIAL("部分支付"),
    CLEARED("已结清"),
    CLOSED("已关闭");

    private final String description;

    LedgerStatus(String description) {
        this.description = description;
    }
}
