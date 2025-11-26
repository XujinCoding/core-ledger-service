package com.coreledger.config.converter;

import com.coreledger.enums.CustomerType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * CustomerType 枚举转换器
 * <p>用于 JPA 实体中 CustomerType 与数据库 TINYINT 的转换</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class CustomerTypeConverter implements AttributeConverter<CustomerType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CustomerType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public CustomerType convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : CustomerType.fromValue(dbData);
    }
}
