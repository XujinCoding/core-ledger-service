package com.coreledger.exception;

import com.coreledger.enums.BusinessCode;

/**
 * 资源不存在异常
 *
 * <p>当查询的资源不存在时抛出此异常</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
public class NotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造函数（使用默认错误码）
     *
     * @param message 错误消息
     */
    public NotFoundException(String message) {
        super(BusinessCode.NOT_FOUND, message);
    }

    /**
     * 构造函数（使用错误码枚举）
     *
     * @param businessCode 错误码枚举
     */
    public NotFoundException(BusinessCode businessCode) {
        super(businessCode);
    }

    /**
     * 构造函数（使用错误码枚举和自定义消息）
     *
     * @param businessCode 错误码枚举
     * @param message 自定义消息
     */
    public NotFoundException(BusinessCode businessCode, String message) {
        super(businessCode, message);
    }
}
