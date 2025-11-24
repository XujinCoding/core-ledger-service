package com.coreledger.config.converter;

import com.coreledger.enums.Status;
import jakarta.persistence.Converter;

/**
 * Status 枚举 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class StatusConverter extends BaseEnumConverter<Status> {

    public StatusConverter() {
        super(Status.class);
    }
}
