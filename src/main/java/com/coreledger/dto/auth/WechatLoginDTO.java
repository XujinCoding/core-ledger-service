package com.coreledger.dto.auth;

import com.coreledger.enums.IdentityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 微信小程序登录请求DTO
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
     * 加密数据（包含手机号等敏感信息）
     */
    @Schema(description = "加密数据", example = "CiyLU1Aw2KjvrjMdj8YKliAjtP4gsMZM...")
    private String encryptedData;

    /**
     * 加密算法的初始向量
     */
    @Schema(description = "加密算法的初始向量", example = "r7BXXKkLb8qrSNn05n0qiA==")
    private String iv;

    /**
     * 微信昵称（可选）
     */
    @Schema(description = "微信昵称", example = "张三")
    private String nickname;

    /**
     * 微信头像URL（可选）
     */
    @Schema(description = "微信头像URL", example = "https://thirdwx.qlogo.cn/...")
    private String avatarUrl;

    /**
     * 身份类型（必需）
     * MERCHANT_OWNER: 商户登录
     * CUSTOMER: 客户登录
     */
    @NotNull(message = "身份类型不能为空")
    @Schema(description = "身份类型", required = true, example = "MERCHANT_OWNER")
    private IdentityType identityType;
}
