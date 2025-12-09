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
@Schema(description = "当前登录用户身份")
public class CurrentUserIdentityInfo {

    /**
     * customer.id / merchant.id 根据身份确定
     *
     */
    @Schema(description = "标识", example = "1")
    private Long id;
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;


    /**
     * customer.name / merchant.name 根据身份确定
     */
    @Schema(description = "名称", example = "admin")
    private String name;

    /**
     * customer.phone / merchant.phone 根据身份确定
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * customer.code/ merchant.code 根据身份确定
     */
    @Schema(description = "编码", example = "xxxxxxxxxxxxxx")
    private String code;

    /**
     * customer.addressId/ merchant.addressId 根据身份确定
     */
    @Schema(description = "关联地址标识", example = "10001")
    private Long addressId;

    /**
     * customer.addressDetail/ merchant.addressDetail 根据身份确定
     */
    @Schema(description = "详细地址", example = "xxxxxx村x号")
    private String addressDetail;

    /**
     * 商户标识, 如果没有商户标识, 标识客户当前没有选择任何商户
     *
     */
    @Schema(description = "商户标识", example = "1")
    private Long merchantId;

    /**
     * 身份类型：MERCHANT_OWNER 或 CUSTOMER
     */
    @Schema(description = "身份类型", example = "MERCHANT_OWNER")
    private IdentityType identityType;
}
