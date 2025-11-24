package com.coreledger.config.typehandler;

import com.coreledger.enums.UserRole;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * UserRole 枚举 MyBatis 类型处理器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@MappedTypes(UserRole.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class UserRoleTypeHandler extends BaseEnumTypeHandler<UserRole> {

    public UserRoleTypeHandler() {
        super(UserRole.class);
    }
}
