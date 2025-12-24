package com.coreledger.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商户详情VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商户详情")
public class MerchantVO {

    /**
     * 商户ID
     */
    @Schema(description = "商户ID")
    private Long id;

    /**
     * 商户编号
     */
    @Schema(description = "商户编号")
    private String code;

    /**
     * 商户名称
     */
    @Schema(description = "商户名称")
    private String name;

    /**
     * 商户所有者User ID
     */
    @Schema(description = "商户所有者User ID")
    private Long ownerUserId;

    /**
     * 邀请码
     */
    @Schema(description = "邀请码")
    private String inviteCode;

    /**
     * 二维码URL
     */
    @Schema(description = "二维码URL")
    private String qrCodeUrl;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 关联地址ID
     */
    @Schema(description = "关联地址ID")
    private Long addressId;

    /**
     * 地址完整路径
     */
    @Schema(description = "地址完整路径")
    private String addressPath;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String addressDetail;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createInstant;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime modifyInstant;
}
