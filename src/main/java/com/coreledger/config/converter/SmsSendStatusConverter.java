package com.coreledger.config.converter;

import com.coreledger.enums.SmsSendStatus;
import jakarta.persistence.Converter;

/**
 * SmsSendStatus 枚举 JPA 转换器
 */
@Converter(autoApply = false)
public class SmsSendStatusConverter extends BaseEnumConverter<SmsSendStatus> {

    public SmsSendStatusConverter() {
        super(SmsSendStatus.class);
    }
}
