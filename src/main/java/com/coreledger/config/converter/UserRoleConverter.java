package com.coreledger.config.converter;

import com.coreledger.enums.UserRole;
import jakarta.persistence.Converter;

/**
 * UserRole 枚举 JPA 转换器
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Converter(autoApply = false)
public class UserRoleConverter extends BaseEnumConverter<UserRole> {

    public UserRoleConverter() {
        super(UserRole.class);
    }
}
