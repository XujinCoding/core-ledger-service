package com.coreledger.config.converter;

import com.coreledger.enums.LedgerStatus;
import jakarta.persistence.Converter;

/**
 * 账本状态枚举 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class LedgerStatusConverter extends BaseEnumConverter<LedgerStatus> {

    public LedgerStatusConverter() {
        super(LedgerStatus.class);
    }
}
