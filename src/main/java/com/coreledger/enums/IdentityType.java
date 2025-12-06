package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户身份类型枚举
 *
 * <p>用于区分用户在系统中的身份：商户所有者或客户</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum IdentityType implements BaseEnum {

    /** 商户所有者 */
    MERCHANT_OWNER(1, "商户所有者"),

    /** 客户 */
    CUSTOMER(2, "客户");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    IdentityType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 CUSTOMER
     */
    public static IdentityType fromValue(int value) {
        for (IdentityType type : IdentityType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return CUSTOMER;
    }
}
