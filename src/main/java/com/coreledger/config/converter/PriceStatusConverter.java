package com.coreledger.config.converter;

import com.coreledger.enums.PriceStatus;
import jakarta.persistence.Converter;

/**
 * 定价状态枚举 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class PriceStatusConverter extends BaseEnumConverter<PriceStatus> {

    public PriceStatusConverter() {
        super(PriceStatus.class);
    }
}
