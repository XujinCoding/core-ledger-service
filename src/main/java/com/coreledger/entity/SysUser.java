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
     * 手机号
     */
    @Column(name = "phone", nullable = false, length = 20, unique = true)
    private String phone;

    /**
     * 角色: 0=普通用户, 1=管理员
     */
    @Column(name = "role", nullable = false)
    @Convert(converter = UserRoleConverter.class)
    private UserRole role = UserRole.USER;

    /**
     * 微信OpenID（唯一标识）
     */
    @Column(name = "wx_openid", length = 100, unique = true)
    private String wxOpenid;

    /**
     * 微信昵称
     */
    @Column(name = "wx_nickname", length = 100)
    private String wxNickname;

    /**
     * 微信头像URL
     */
    @Column(name = "wx_avatar_url", length = 500)
    private String wxAvatarUrl;

    /**
     * 判断是否为管理员
     *
     * @return true=管理员, false=普通用户
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }
}
