package com.coreledger.enums;

import lombok.Getter;

/**
 * 短信发送状态枚举
 */
@Getter
public enum SmsSendStatus implements BaseEnum {

    PENDING(0, "待发送"),
    SUCCESS(1, "发送成功"),
    FAILED(2, "发送失败");

    private final int value;
    private final String description;

    SmsSendStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
