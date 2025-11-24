package com.coreledger.config.converter;

import com.coreledger.enums.LedgerStatus;
import jakarta.persistence.Converter;

/**
 * LedgerStatus 枚举 JPA 转换器
 *
 * <p>用于 JPA 实体中 LedgerStatus 枚举与数据库 TINYINT 类型的相互转换</p>
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
