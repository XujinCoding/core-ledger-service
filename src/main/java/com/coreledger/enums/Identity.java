package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户在商户中的身份枚举
 *
 * @author Core Ledger Team
 * @since 2.0.0
 */
@Getter
@RequiredArgsConstructor
public enum Identity {
    /**
     * 商户老板
     */
    OWNER(0, "商户老板"),

    /**
     * 员工
     */
    EMPLOYEE(1, "员工"),

    /**
     * 客户
     */
    CUSTOMER(2, "客户");

    @JsonValue
    private final Integer code;
    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 编码
     * @return 枚举值
     */
    public static Identity fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (Identity identity : values()) {
            if (identity.code.equals(code)) {
                return identity;
            }
        }
        throw new IllegalArgumentException("Invalid Identity code: " + code);
    }
}
