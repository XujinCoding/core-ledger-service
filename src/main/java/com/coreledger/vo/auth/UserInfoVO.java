package com.coreledger.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "用户信息")
public class UserInfoVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 角色: 0=普通用户, 1=管理员
     */
    @Schema(description = "角色", example = "0")
    private Integer role;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "普通用户")
    private String roleDesc;

    /**
     * 微信昵称
     */
    @Schema(description = "微信昵称", example = "张三")
    private String wxNickname;

    /**
     * 微信头像URL
     */
    @Schema(description = "微信头像URL", example = "https://thirdwx.qlogo.cn/...")
    private String wxAvatarUrl;
}
