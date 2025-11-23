package com.coreledger.enums;

import lombok.Getter;

/**
 * Gender Enum
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum Gender {
    MALE("男"),
    FEMALE("女"),
    UNKNOWN("未知");

    private final String description;

    Gender(String description) {
        this.description = description;
    }
}
