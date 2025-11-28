package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 操作类型枚举
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum OperationType implements BaseEnum {

    /** 创建 */
    CREATE(1, "创建"),

    /** 更新 */
    UPDATE(2, "更新"),

    /** 删除 */
    DELETE(3, "删除");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    OperationType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 CREATE
     */
    public static OperationType fromValue(int value) {
        for (OperationType type : OperationType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return CREATE;
    }
}
