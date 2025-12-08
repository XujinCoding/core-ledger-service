package com.coreledger.vo.auth;

import com.coreledger.entity.Merchant;
import com.coreledger.enums.IdentityType;
import com.coreledger.vo.customer.CustomerVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
     * Token过期时间
     */
    @Schema(description = "Token过期时间", example = "2025-12-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 是否需要注册身份
     * true: 用户没有对应身份，需要调用注册接口
     * false: 已有身份信息或身份列表
     */
    @Schema(description = "是否需要注册身份", example = "false")
    private Boolean needRegister;

    /**
     * 当前的身份类型
     * MERCHANT_OWNER 或 CUSTOMER
     */
    @Schema(description = "当前的身份类型", example = "MERCHANT_OWNER")
    private IdentityType registerType;

    /**
     * 商户列表（多个商户时返回）
     */
    @Schema(description = "商户列表")
    private List<Merchant> merchants;

    /**
     * 客户列表（多个客户时返回）
     */
    @Schema(description = "客户列表")
    private List<CustomerVO> customers;

    /**
     * 提示信息
     */
    @Schema(description = "提示信息", example = "请选择商户")
    private String message;

    /**
     * 创建成功响应（登录成功）
     */
    public static LoginVO success(String token, UserInfoVO userInfo, LocalDateTime expireTime) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(userInfo);
        vo.setExpireTime(expireTime);
        return vo;
    }
}
