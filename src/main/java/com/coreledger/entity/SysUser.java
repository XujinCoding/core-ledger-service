package com.coreledger.entity;

import com.coreledger.config.converter.UserRoleConverter;
import com.coreledger.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 系统用户实体
 *
 * <p>对应数据库表: sys_user</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {

    /**
     * 用户名（可选，管理员必填）
     */
    @Column(name = "username", length = 50, unique = true)
    private String username;

    /**
     * 密码（BCrypt加密，管理员必填）
     */
    @Column(name = "password", length = 100)
    private String password;

    /**
     * 微信OpenID（唯一标识）
     */
    @Column(name = "wx_openid", length = 100, unique = true)
    private String wxOpenid;
}
