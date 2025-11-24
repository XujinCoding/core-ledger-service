package com.coreledger.exception;

import com.coreledger.enums.BusinessCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * <p>用于业务逻辑中的异常处理</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造函数（使用错误码枚举）
     *
     * @param businessCode 错误码枚举
     */
    public BusinessException(BusinessCode businessCode) {
        super(businessCode.getMessage());
        this.code = businessCode.getCode();
        this.message = businessCode.getMessage();
    }

    /**
     * 构造函数（使用错误码枚举和自定义消息）
     *
     * @param businessCode 错误码枚举
     * @param message 自定义消息
     */
    public BusinessException(BusinessCode businessCode, String message) {
        super(message);
        this.code = businessCode.getCode();
        this.message = message;
    }

    /**
     * 构造函数（使用错误码和消息）
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数（使用错误码枚举和异常原因）
     *
     * @param businessCode 错误码枚举
     * @param cause 异常原因
     */
    public BusinessException(BusinessCode businessCode, Throwable cause) {
        super(businessCode.getMessage(), cause);
        this.code = businessCode.getCode();
        this.message = businessCode.getMessage();
    }
}
