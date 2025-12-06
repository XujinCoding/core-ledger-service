package com.coreledger.config.typehandler;

import com.coreledger.enums.IdentityType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * IdentityType 枚举 MyBatis 类型处理器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(IdentityType.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class IdentityTypeTypeHandler extends BaseEnumTypeHandler<IdentityType> {

    public IdentityTypeTypeHandler() {
        super(IdentityType.class);
    }
}
