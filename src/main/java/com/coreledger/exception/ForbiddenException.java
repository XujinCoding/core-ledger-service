package com.coreledger.exception;

import com.coreledger.enums.BusinessCode;

/**
 * 无权限异常
 *
 * <p>当用户无权限访问资源时抛出此异常</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
public class ForbiddenException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数（使用默认错误码）
     *
     * @param message 错误消息
     */
    public ForbiddenException(String message) {
        super(BusinessCode.FORBIDDEN, message);
    }

    /**
     * 构造函数（使用默认错误码和消息）
     */
    public ForbiddenException() {
        super(BusinessCode.FORBIDDEN);
    }

    /**
     * 构造函数（使用错误码枚举）
     *
     * @param businessCode 错误码枚举
     */
    public ForbiddenException(BusinessCode businessCode) {
        super(businessCode);
    }
}
