package com.coreledger.config.converter;

import com.coreledger.enums.SmsScene;
import jakarta.persistence.Converter;

/**
 * SmsScene 枚举 JPA 转换器
 */
@Converter(autoApply = false)
public class SmsSceneConverter extends BaseEnumConverter<SmsScene> {

    public SmsSceneConverter() {
        super(SmsScene.class);
    }
}
