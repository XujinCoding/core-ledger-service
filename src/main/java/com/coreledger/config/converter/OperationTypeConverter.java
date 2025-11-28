package com.coreledger.config.converter;

import com.coreledger.enums.OperationType;
import jakarta.persistence.Converter;

/**
 * 操作类型 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class OperationTypeConverter extends BaseEnumConverter<OperationType> {

    public OperationTypeConverter() {
        super(OperationType.class);
    }
}
