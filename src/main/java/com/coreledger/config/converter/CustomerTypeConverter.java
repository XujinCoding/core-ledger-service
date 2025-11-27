package com.coreledger.config.converter;

import com.coreledger.enums.CustomerType;
import jakarta.persistence.Converter;

/**
 * 客户类型枚举 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class CustomerTypeConverter extends BaseEnumConverter<CustomerType> {

    public CustomerTypeConverter() {
        super(CustomerType.class);
    }
}
