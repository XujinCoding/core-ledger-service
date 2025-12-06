package com.coreledger.vo.auth;

import com.coreledger.enums.IdentityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "用户信息")
public class UserInfoVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 角色: 0=普通用户, 1=管理员
     */
    @Schema(description = "角色", example = "0")
    private Integer role;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "普通用户")
    private String roleDesc;

    /**
     * 微信昵称
     */
    @Schema(description = "微信昵称", example = "张三")
    private String wxNickname;

    /**
     * 微信头像URL
     */
    @Schema(description = "微信头像URL", example = "https://thirdwx.qlogo.cn/...")
    private String wxAvatarUrl;

    /**
     * 商户ID
     */
    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    /**
     * 商户名称
     */
    @Schema(description = "商户名称", example = "张三小店")
    private String merchantName;

    /**
     * 商户编号
     */
    @Schema(description = "商户编号", example = "M_20251206_001")
    private String merchantNo;

    /**
     * 客户ID
     */
    @Schema(description = "客户ID", example = "1")
    private Long customerId;

    /**
     * 客户名称
     */
    @Schema(description = "客户名称", example = "李四")
    private String customerName;

    /**
     * 客户编号
     */
    @Schema(description = "客户编号", example = "C_20251206_001")
    private String customerNo;

    /**
     * 客户手机号
     */
    @Schema(description = "客户手机号", example = "13800138000")
    private String customerPhone;

    /**
     * 身份类型：MERCHANT_OWNER 或 CUSTOMER
     */
    @Schema(description = "身份类型", example = "MERCHANT_OWNER")
    private IdentityType identityType;

    /**
     * 身份类型值（用于 JSON 序列化）
     */
    public Integer getIdentityTypeValue() {
        return identityType != null ? identityType.getValue() : null;
    }
}
