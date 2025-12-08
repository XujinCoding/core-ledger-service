package com.coreledger.dto.auth;

import com.coreledger.enums.IdentityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 微信小程序登录请求DTO
 * 仅用于获取微信用户信息，具体的用户信息（手机号、用户名、密码等）在注册时填写
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "微信小程序登录请求")
public class WechatLoginDTO {

    /**
     * 微信登录凭证code
     */
    @NotBlank(message = "微信登录凭证不能为空")
    @Schema(description = "微信登录凭证code", required = true, example = "071Ab2Ga1n8YYJ0MJVIa1Ht9Ga1Ab2G5")
    private String code;

    /**
     * 身份类型（必需）
     * MERCHANT_OWNER: 商户登录
     * CUSTOMER: 客户登录
     */
    @NotNull(message = "身份类型不能为空")
    @Schema(description = "身份类型", required = true, example = "MERCHANT_OWNER")
    private IdentityType identityType;
}
