package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 支付方式枚举
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum PaymentMethod implements BaseEnum {

    /** 现金 */
    CASH(1, "现金"),

    /** 微信 */
    WECHAT(2, "微信"),

    /** 支付宝 */
    ALIPAY(3, "支付宝"),

    /** 银行转账 */
    BANK_TRANSFER(4, "银行转账");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    PaymentMethod(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 CASH
     */
    public static PaymentMethod fromValue(int value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value == value) {
                return method;
            }
        }
        return CASH;
    }
}
