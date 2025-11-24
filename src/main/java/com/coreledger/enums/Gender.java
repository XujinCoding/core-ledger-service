package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum Gender implements BaseEnum {

    /** 未知 */
    UNKNOWN(0, "未知"),

    /** 男 */
    MALE(1, "男"),

    /** 女 */
    FEMALE(2, "女");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    Gender(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 UNKNOWN
     */
    public static Gender fromValue(int value) {
        for (Gender gender : Gender.values()) {
            if (gender.value == value) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
