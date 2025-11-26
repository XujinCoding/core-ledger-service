package com.coreledger.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 补充用户信息请求DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "补充用户信息请求")
public class SupplementUserInfoDTO {

    /**
     * 微信OpenID（用于已存在用户）或临时OpenID（用于新用户注册）
     */
    @NotBlank(message = "OpenID不能为空")
    @Schema(description = "微信OpenID", required = true, example = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o")
    private String openid;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true, example = "13800138000")
    private String phone;

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
     * 用户名（可选）
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;
}
