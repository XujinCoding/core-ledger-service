package com.coreledger.dto.auth;

import com.coreledger.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 客户注册DTO
 * 包含用户通用信息和客户专属信息
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户注册请求")
public class CustomerRegisterDTO {

    /**
     * 微信OpenID
     */
    @NotBlank(message = "微信OpenID不能为空")
    @Schema(description = "微信OpenID", example = "oXXXXXXXXXXXXXXXXXXXXXXXXXX")
    private String openid;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 微信昵称
     */
    @Schema(description = "微信昵称", example = "李四")
    private String nickname;

    /**
     * 微信头像URL
     */
    @Schema(description = "微信头像URL", example = "https://thirdwx.qlogo.cn/...")
    private String avatarUrl;

    /**
     * 客户姓名（客户专属信息）
     */
    @NotBlank(message = "客户姓名不能为空")
    @Schema(description = "客户姓名", example = "李四")
    private String customerName;

    /**
     * 别名/昵称（客户专属信息）
     */
    @Schema(description = "别名/昵称", example = "小李")
    private String alias;

    /**
     * 性别（客户专属信息）
     */
    @Schema(description = "性别", example = "MALE")
    private Gender gender;

    /**
     * 年龄（客户专属信息）
     */
    @Schema(description = "年龄", example = "28")
    private Integer age;

    /**
     * 地址ID（客户专属信息）
     */
    @NotNull(message = "地址ID不能为空")
    @Schema(description = "地址ID", example = "1")
    private Long addressId;

    /**
     * 详细地址（客户专属信息）
     */
    @Schema(description = "详细地址", example = "北京市朝阳区某某街道")
    private String addressDetail;

    /**
     * 商户邀请码
     */
    @Schema(description = "商户邀请码", example = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
    private String inviteCode;
}
