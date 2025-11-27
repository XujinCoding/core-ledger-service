package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 定价状态枚举
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum PriceStatus implements BaseEnum {

    /** 未定价 */
    UNPRICED(0, "未定价"),

    /** 已定价 */
    PRICED(1, "已定价");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    PriceStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 UNPRICED
     */
    public static PriceStatus fromValue(int value) {
        for (PriceStatus status : PriceStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return UNPRICED;
    }
}
