package com.coreledger.vo.auth;

import com.coreledger.entity.Customer;
import com.coreledger.entity.Merchant;
import com.coreledger.enums.IdentityType;
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
     * 是否需要补充信息
     */
    @Schema(description = "是否需要补充信息", example = "false")
    private Boolean needSupplement;

    /**
     * 是否为新用户（需要注册）
     */
    @Schema(description = "是否为新用户", example = "false")
    private Boolean isNewUser;

    /**
     * 临时OpenID（需要补充信息或注册时返回）
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
     * 是否需要注册身份
     * true: 用户没有对应身份，需要调用注册接口
     * false: 已有身份信息或身份列表
     */
    @Schema(description = "是否需要注册身份", example = "false")
    private Boolean needRegister;

    /**
     * 需要注册的身份类型
     * MERCHANT_OWNER 或 CUSTOMER
     */
    @Schema(description = "需要注册的身份类型", example = "MERCHANT_OWNER")
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
    private List<Customer> customers;

    /**
     * 已选中的商户ID
     */
    @Schema(description = "已选中的商户ID", example = "100")
    private Long selectedMerchantId;

    /**
     * 已选中的客户ID
     */
    @Schema(description = "已选中的客户ID", example = "200")
    private Long selectedCustomerId;

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
        vo.setNeedSupplement(false);
        vo.setIsNewUser(false);
        vo.setExpireTime(expireTime);
        return vo;
    }

    /**
     * 创建需要补充信息的响应（已存在用户）
     */
    public static LoginVO needSupplement(String tempOpenid) {
        LoginVO vo = new LoginVO();
        vo.setNeedSupplement(true);
        vo.setIsNewUser(false);
        vo.setTempOpenid(tempOpenid);
        return vo;
    }

    /**
     * 创建需要注册的响应（新用户）
     */
    public static LoginVO needRegister(String tempOpenid) {
        LoginVO vo = new LoginVO();
        vo.setNeedSupplement(true);
        vo.setIsNewUser(true);
        vo.setTempOpenid(tempOpenid);
        return vo;
    }
}
