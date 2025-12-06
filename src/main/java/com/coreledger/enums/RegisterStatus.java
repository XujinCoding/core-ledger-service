package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 客户注册状态枚举
 *
 * <p>用于标记客户是否已注册（是否关联了用户账户）</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum RegisterStatus implements BaseEnum {

    /** 未注册 */
    UNREGISTERED(0, "未注册"),

    /** 已注册 */
    REGISTERED(1, "已注册");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    RegisterStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 UNREGISTERED
     */
    public static RegisterStatus fromValue(int value) {
        for (RegisterStatus status : RegisterStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return UNREGISTERED;
    }
}
