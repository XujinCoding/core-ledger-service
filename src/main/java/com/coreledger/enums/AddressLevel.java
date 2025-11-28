package com.coreledger.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 地址层级枚举
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public enum AddressLevel implements BaseEnum {

    /** 省 */
    PROVINCE(1, "省"),

    /** 市 */
    CITY(2, "市"),

    /** 区县 */
    DISTRICT(3, "区县"),

    /** 镇/乡/街道 */
    TOWN(4, "镇/乡/街道"),

    /** 村 */
    VILLAGE(5, "村");

    /** 枚举值（数据库存储） */
    @JsonValue
    private final int value;

    /** 描述 */
    private final String description;

    AddressLevel(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据枚举值获取枚举对象
     *
     * @param value 枚举值
     * @return 枚举对象，未找到时返回 PROVINCE
     */
    public static AddressLevel fromValue(int value) {
        for (AddressLevel level : AddressLevel.values()) {
            if (level.value == value) {
                return level;
            }
        }
        return PROVINCE;
    }

    /**
     * 判断是否为村级及以上
     *
     * @return true=村级及以上, false=村级以下
     */
    public boolean isVillageOrAbove() {
        return this.value >= TOWN.value;
    }
}
