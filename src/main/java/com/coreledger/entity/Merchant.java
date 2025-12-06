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
    @Column(name = "merchant_no", nullable = false, length = 32, unique = true)
    private String merchantNo;

    /** 商户名称 */
    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    /** 商户所有者User ID */
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    /** 邀请码，用于生成二维码 */
    @Column(name = "invite_code", nullable = false, length = 20, unique = true)
    private String inviteCode;

    /** 二维码URL */
    @Column(name = "qr_code_url", length = 500)
    private String qrCodeUrl;
}
