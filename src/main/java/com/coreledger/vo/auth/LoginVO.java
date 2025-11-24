package com.coreledger.vo.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录响应VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "登录响应")
public class LoginVO {

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfoVO userInfo;

    /**
     * 是否需要绑定手机号
     */
    @Schema(description = "是否需要绑定手机号", example = "false")
    private Boolean needBindPhone;

    /**
     * 临时OpenID（需要绑定手机号时返回）
     */
    @Schema(description = "临时OpenID", example = "oUpF8uMuAJO_M2pxb1Q9zNjWeS6o")
    private String tempOpenid;

    /**
     * Token过期时间
     */
    @Schema(description = "Token过期时间", example = "2025-12-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 创建成功响应（登录成功）
     */
    public static LoginVO success(String token, UserInfoVO userInfo, LocalDateTime expireTime) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(userInfo);
        vo.setNeedBindPhone(false);
        vo.setExpireTime(expireTime);
        return vo;
    }

    /**
     * 创建需要绑定手机号的响应
     */
    public static LoginVO needBindPhone(String tempOpenid) {
        LoginVO vo = new LoginVO();
        vo.setNeedBindPhone(true);
        vo.setTempOpenid(tempOpenid);
        return vo;
    }
}
