package com.coreledger.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 商户注册DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商户注册请求")
public class MerchantRegisterDTO {

    /**
     * 微信Code
     */
    @NotBlank(message = "微信登录凭证不能为空")
    @Schema(description = "微信登录凭证code", required = true, example = "071Ab2Ga1n8YYJ0MJVIa1Ht9Ga1Ab2G5")
    private String code;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 短信验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4,6}$", message = "验证码格式不正确")
    @Schema(description = "短信验证码", required = true, example = "123456")
    private String smsCode;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "merchant_001")
    private String username;

    /**
     * 密码（需要加密传输）
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "encrypted_password_hash")
    private String password;

    /**
     * 商户名称
     */
    @NotBlank(message = "商户名称不能为空")
    @Schema(description = "商户名称", example = "张三便利店")
    private String merchantName;

    /**
     * 微信昵称
     */
    @Schema(description = "微信昵称", example = "张三")
    private String nickname;

    /**
     * 微信头像URL
     */
    @Schema(description = "微信头像URL", example = "https://thirdwx.qlogo.cn/...")
    private String avatarUrl;

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
}
