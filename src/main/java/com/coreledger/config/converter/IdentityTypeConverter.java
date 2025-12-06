package com.coreledger.config.converter;

import com.coreledger.enums.IdentityType;
import jakarta.persistence.Converter;

/**
 * IdentityType 枚举 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class IdentityTypeConverter extends BaseEnumConverter<IdentityType> {

    public IdentityTypeConverter() {
        super(IdentityType.class);
    }
}
