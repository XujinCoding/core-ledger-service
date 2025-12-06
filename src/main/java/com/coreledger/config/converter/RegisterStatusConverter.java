package com.coreledger.config.converter;

import com.coreledger.enums.RegisterStatus;
import jakarta.persistence.Converter;

/**
 * RegisterStatus 枚举 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class RegisterStatusConverter extends BaseEnumConverter<RegisterStatus> {

    public RegisterStatusConverter() {
        super(RegisterStatus.class);
    }
}
