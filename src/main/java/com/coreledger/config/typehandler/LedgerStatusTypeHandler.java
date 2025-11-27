package com.coreledger.config.typehandler;

import com.coreledger.enums.LedgerStatus;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 账本状态枚举 MyBatis 类型处理器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(LedgerStatus.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class LedgerStatusTypeHandler extends BaseEnumTypeHandler<LedgerStatus> {

    public LedgerStatusTypeHandler() {
        super(LedgerStatus.class);
    }
}
