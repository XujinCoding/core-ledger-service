package com.coreledger.exception;

import com.coreledger.enums.BusinessCode;

/**
 * 未授权异常
 *
 * <p>当用户未登录或登录已过期时抛出此异常</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
public class UnauthorizedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数（使用默认错误码）
     *
     * @param message 错误消息
     */
    public UnauthorizedException(String message) {
        super(BusinessCode.UNAUTHORIZED, message);
    }

    /**
     * 构造函数（使用默认错误码和消息）
     */
    public UnauthorizedException() {
        super(BusinessCode.UNAUTHORIZED);
    }
}
