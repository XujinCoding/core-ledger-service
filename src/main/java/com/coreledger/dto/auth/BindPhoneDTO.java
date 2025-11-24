package com.coreledger.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 绑定手机号请求DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "绑定手机号请求")
public class BindPhoneDTO {

    /**
     * 微信OpenID（临时凭证）
     */
    @NotBlank(message = "OpenID不能为空")
    @Schema(description = "微信OpenID", example = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o")
    private String openid;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 加密数据（微信返回的手机号加密数据）
     */
    @Schema(description = "加密数据", example = "CiyLU1Aw2KjvrjMdj8YKliAjtP4gsMZM...")
    private String encryptedData;

    /**
     * 加密算法的初始向量
     */
    @Schema(description = "加密算法的初始向量", example = "r7BXXKkLb8qrSNn05n0qiA==")
    private String iv;
}
