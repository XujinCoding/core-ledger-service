package com.coreledger.enums;

import lombok.Getter;

/**
 * Payment Method Enum
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum PaymentMethod {
    WECHAT("微信支付"),
    CASH("现金");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}
