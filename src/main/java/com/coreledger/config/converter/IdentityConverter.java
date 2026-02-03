package com.coreledger.config.converter;

import com.coreledger.enums.Identity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Identity 枚举转换器
 *
 * @author Core Ledger Team
 * @since 2.0.0
 */
@Converter(autoApply = true)
public class IdentityConverter implements AttributeConverter<Identity, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Identity identity) {
        return identity == null ? null : identity.getCode();
    }

    @Override
    public Identity convertToEntityAttribute(Integer code) {
        return code == null ? null : Identity.fromCode(code);
    }
}
