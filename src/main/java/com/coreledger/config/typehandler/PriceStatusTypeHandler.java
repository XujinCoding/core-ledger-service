package com.coreledger.config.typehandler;

import com.coreledger.enums.PriceStatus;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 定价状态枚举 MyBatis 类型处理器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(PriceStatus.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class PriceStatusTypeHandler extends BaseEnumTypeHandler<PriceStatus> {

    public PriceStatusTypeHandler() {
        super(PriceStatus.class);
    }
}
