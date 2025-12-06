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
     * 设置用户名
     */
    public static void setUsername(String username) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setUsername(username);
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getUsername() : null;
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
     * 设置商户名称
     */
    public static void setMerchantName(String merchantName) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setMerchantName(merchantName);
    }

    /**
     * 获取商户名称
     */
    public static String getMerchantName() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getMerchantName() : null;
    }

    /**
     * 设置商户编号
     */
    public static void setMerchantNo(String merchantNo) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setMerchantNo(merchantNo);
    }

    /**
     * 获取商户编号
     */
    public static String getMerchantNo() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getMerchantNo() : null;
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
     * 设置客户名称
     */
    public static void setCustomerName(String customerName) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setCustomerName(customerName);
    }

    /**
     * 获取客户名称
     */
    public static String getCustomerName() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getCustomerName() : null;
    }

    /**
     * 设置客户编号
     */
    public static void setCustomerNo(String customerNo) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setCustomerNo(customerNo);
    }

    /**
     * 获取客户编号
     */
    public static String getCustomerNo() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getCustomerNo() : null;
    }

    /**
     * 设置客户手机号
     */
    public static void setCustomerPhone(String customerPhone) {
        SessionInfo info = getOrCreateSessionInfo();
        info.setCustomerPhone(customerPhone);
    }

    /**
     * 获取客户手机号
     */
    public static String getCustomerPhone() {
        SessionInfo info = SESSION_INFO.get();
        return info != null ? info.getCustomerPhone() : null;
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
