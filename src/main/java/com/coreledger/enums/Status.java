package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 状态枚举
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum Status implements BaseEnum {

    /** 无效/已删除 */
    INACTIVE(0, "无效"),

    /** 有效/启用 */
    ACTIVE(1, "有效");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    Status(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 INACTIVE
     */
    public static Status fromValue(int value) {
        for (Status status : Status.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return INACTIVE;
    }
}
