package com.coreledger.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
}
