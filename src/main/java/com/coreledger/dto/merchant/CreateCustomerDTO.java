package com.coreledger.dto.merchant;

import com.coreledger.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建客户DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "创建客户请求")
public class CreateCustomerDTO {

    /**
     * 商户ID
     */
    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    /**
     * 客户名称
     */
    @Schema(description = "客户名称", example = "王五")
    private String name;

    /**
     * 别名/昵称
     */
    @Schema(description = "别名/昵称", example = "小王")
    private String alias;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    /**
     * 地址
     */
    @Schema(description = "地址标识", example = "10000")
    private Long addressId;

    /**
     * 地址详情
     */
    @Schema(description = "地址", example = "北京市朝阳区")
    private String addressDetail;

    /**
     * 性别：0=未知, 1=男, 2=女
     */
    @Schema(description = "性别", example = "1")
    private Gender gender;

    /**
     * 年龄
     */
    @Schema(description = "年龄", example = "30")
    private Integer age;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "VIP客户")
    private String remark;

    /**
     * 客户头像URL
     */
    @Schema(description = "客户头像URL")
    private String avatarUrl;
}
