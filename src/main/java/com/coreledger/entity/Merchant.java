package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 商户信息实体
 *
 * <p>对应数据库表: merchant</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "merchant")
public class Merchant extends BaseEntity {

    /** 商户编号，格式：M_yyyyMMddHHmmss_随机3位 */
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    /** 商户名称 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 商户所有者User ID */
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    /** 邀请码，用于生成二维码 */
    @Column(name = "invite_code", nullable = false, length = 20, unique = true)
    private String inviteCode;

    /** 二维码URL */
    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;

    /** 手机号 */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 关联地址ID */
    @Column(name = "address_id", nullable = false)
    private Long addressId;

    /** 详细地址 */
    @Column(name = "address_detail", length = 255)
    private String addressDetail;
}
