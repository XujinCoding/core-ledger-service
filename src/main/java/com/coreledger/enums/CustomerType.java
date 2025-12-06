package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 客户类型枚举
 * TEMPLATE: 模板客户（用户首次注册时创建，未绑定商户）
 * FORMAL: 正式客户（绑定到具体商户）
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum CustomerType implements BaseEnum {

    /** 模板客户（未绑定商户） */
    TEMPLATE(0, "模板客户"),

    /** 正式客户（已绑定商户） */
    FORMAL(1, "正式客户");

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
     * @return 枚举对象，未找到时返回 FORMAL
     */
    public static CustomerType fromValue(int value) {
        for (CustomerType type : CustomerType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return FORMAL;
    }
}
