package com.coreledger.config.typehandler;

import com.coreledger.enums.CustomerType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 客户类型枚举 MyBatis 类型处理器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(CustomerType.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class CustomerTypeTypeHandler extends BaseEnumTypeHandler<CustomerType> {

    public CustomerTypeTypeHandler() {
        super(CustomerType.class);
    }
}
