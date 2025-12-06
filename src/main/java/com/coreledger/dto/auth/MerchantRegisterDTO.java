package com.coreledger.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "微信OpenID", example = "oXXXXXXXXXXXXXXXXXXXXXXXXXX")
    private String openid;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "merchant_001")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 商户名称
     */
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
