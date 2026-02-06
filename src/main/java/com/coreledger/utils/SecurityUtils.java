package com.coreledger.utils;

import com.coreledger.enums.IdentityType;
import com.coreledger.exception.UnauthorizedException;
import com.coreledger.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 提供便捷方法从 Spring Security 上下文获取当前用户信息
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
public class SecurityUtils {

    /**
     * 获取当前用户主体
     *
     * @return 当前用户主体
     * @throws UnauthorizedException 如果未登录或登录已过期
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null &&
                authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }

        throw new UnauthorizedException("未登录或登录已过期");
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     * @throws UnauthorizedException 如果未登录或登录已过期
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * 获取当前商户ID
     *
     * @return 商户ID，如果未设置则返回 null
     */
    public static Long getCurrentMerchantId() {
        return getCurrentUser().getMerchantId();
    }

    /**
     * 获取当前客户ID
     *
     * @return 客户ID，如果未设置则返回 null
     */
    public static Long getCurrentCustomerId() {
        return getCurrentUser().getCustomerId();
    }

    /**
     * 获取当前身份类型
     *
     * @return 身份类型，如果未设置则返回 null
     */
    public static IdentityType getCurrentIdentityType() {
        return getCurrentUser().getIdentityType();
    }

    /**
     * 获取当前Token
     *
     * @return Token
     * @throws UnauthorizedException 如果未登录或登录已过期
     */
    public static String getCurrentToken() {
        return getCurrentUser().getToken();
    }

    /**
     * 检查是否已选择身份
     *
     * @return true 如果已选择身份，false 否则
     */
    public static boolean hasSelectedIdentity() {
        try {
            return getCurrentUser().hasSelectedIdentity();
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    /**
     * 检查是否是商户身份
     *
     * @return true 如果是商户身份，false 否则
     */
    public static boolean isMerchantOwner() {
        try {
            UserPrincipal user = getCurrentUser();
            return user.isMerchantOwner() && user.getMerchantId() != null;
        } catch (UnauthorizedException e) {
            return false;
        }
    }

    /**
     * 检查是否是客户身份
     *
     * @return true 如果是客户身份，false 否则
     */
    public static boolean isCustomer() {
        try {
            UserPrincipal user = getCurrentUser();
            return user.isCustomer() && user.getCustomerId() != null;
        } catch (UnauthorizedException e) {
            return false;
        }
    }
}
