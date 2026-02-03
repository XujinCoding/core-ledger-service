package com.coreledger.entity;

import com.coreledger.config.converter.GenderConverter;
import com.coreledger.enums.Gender;
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

    /**
     * 真实姓名
     */
    @Column(name = "name", length = 100)
    private String name;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /**
     * 用户头像URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 性别
     */
    @Column(name = "gender")
    @Convert(converter = GenderConverter.class)
    private Gender gender = Gender.UNKNOWN;

    /**
     * 年龄
     */
    @Column(name = "age")
    private Integer age;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20, unique = true)
    private String phone;

    /**
     * 关联地址ID
     */
    @Column(name = "address_id")
    private Long addressId;

    /**
     * 详细地址
     */
    @Column(name = "address_detail", length = 255)
    private String addressDetail;
}
