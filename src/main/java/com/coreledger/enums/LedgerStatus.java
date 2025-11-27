package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 账本状态枚举
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum LedgerStatus implements BaseEnum {

    /** 进行中 */
    IN_PROGRESS(1, "进行中"),

    /** 部分缴费 */
    PARTIAL(2, "部分缴费"),

    /** 已结清 */
    CLEARED(3, "已结清"),

    /** 赊账中 */
    ON_CREDIT(4, "赊账中"),

    /** 已关闭 */
    CLOSED(5, "已关闭");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    LedgerStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 IN_PROGRESS
     */
    public static LedgerStatus fromValue(int value) {
        for (LedgerStatus status : LedgerStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return IN_PROGRESS;
    }
}
