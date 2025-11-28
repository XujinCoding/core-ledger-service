package com.coreledger.config.typehandler;

import com.coreledger.enums.OperationType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 操作类型 MyBatis 类型处理器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(OperationType.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class OperationTypeTypeHandler extends BaseEnumTypeHandler<OperationType> {

    public OperationTypeTypeHandler() {
        super(OperationType.class);
    }
}
