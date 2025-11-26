package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 客户类型枚举
 *
 * <p>业务说明:</p>
 * <ul>
 *   <li><b>ACTIVE (活跃客户)</b>: 正常交易的客户</li>
 *   <li><b>POTENTIAL (潜在客户)</b>: 已删除或不活跃的客户</li>
 * </ul>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum CustomerType implements BaseEnum {

    /** 潜在客户 */
    POTENTIAL(0, "潜在客户"),

    /** 活跃客户 */
    ACTIVE(1, "活跃客户");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    CustomerType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 ACTIVE
     */
    public static CustomerType fromValue(int value) {
        for (CustomerType type : CustomerType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return ACTIVE;
    }

    /**
     * 判断是否为活跃客户
     *
     * @return true 如果是活跃客户
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
}
