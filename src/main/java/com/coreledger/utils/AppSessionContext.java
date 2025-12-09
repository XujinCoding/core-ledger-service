package com.coreledger.utils;

import com.coreledger.enums.IdentityType;
import com.coreledger.vo.session.SessionInfo;

/**
 * 应用会话上下文
 * 使用 ThreadLocal 存储当前用户的会话信息
 * 支持存储用户、商户、客户等详细信息
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
public class AppSessionContext {

    private static final ThreadLocal<SessionInfo> SESSION_INFO = new ThreadLocal<>();

    /**
     * 设置会话信息
     */
    public static void setSessionInfo(SessionInfo sessionInfo) {
        SESSION_INFO.set(sessionInfo);
    }

    /**
     * 获取会话信息
     */
    public static SessionInfo getSessionInfo() {
        return SESSION_INFO.get();
    }

    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setUserId(userId);
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getUserId() : null;
    }

    /**
     * 设置用户ID
     */
    public static void setToken(String token) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setToken(token);
    }

    /**
     * 获取用户ID
     */
    public static String getToken() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getToken() : null;
    }


    /**
     * 设置商户ID
     */
    public static void setMerchantId(Long merchantId) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setMerchantId(merchantId);
    }

    /**
     * 获取商户ID
     */
    public static Long getMerchantId() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getMerchantId() : null;
    }

    /**
     * 设置客户ID
     */
    public static void setCustomerId(Long customerId) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setCustomerId(customerId);
    }

    /**
     * 获取客户ID
     */
    public static Long getCustomerId() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getCustomerId() : null;
    }

    /**
     * 设置身份类型
     */
    public static void setIdentityType(IdentityType identityType) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setIdentityType(identityType);
    }

    /**
     * 获取身份类型
     */
    public static IdentityType getIdentityType() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getIdentityType() : null;
    }

    /**
     * 获取或创建会话信息
     */
    private static SessionInfo getOrCreateSessionInfo() {
        SessionInfo info = SESSION_INFO.get();
        if (info == null) {
            info = new SessionInfo();
            SESSION_INFO.set(info);
        }
        return info;
    }

    /**
     * 清空所有会话信息
     */
    public static void clear() {
        SESSION_INFO.remove();
    }
}
