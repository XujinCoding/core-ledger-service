package com.coreledger.enums;

import lombok.Getter;

/**
 * 短信场景枚举
 */
@Getter
public enum SmsScene implements BaseEnum {

    MERCHANT_REGISTER(1, "商户注册"),
    CUSTOMER_REGISTER(2, "客户注册"),
    LOGIN(3, "登录验证"),
    RESET_PASSWORD(4, "重置密码"),
    CHANGE_PHONE(5, "修改手机号");

    private final int value;
    private final String description;

    SmsScene(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
