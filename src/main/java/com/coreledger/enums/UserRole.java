package com.coreledger.enums;

import lombok.Getter;

/**
 * User Role Enum
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum UserRole {
    ADMIN("管理员"),
    USER("普通用户");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
