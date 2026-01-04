package com.coreledger.common;

import com.coreledger.enums.BusinessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装
 *
 * <p>所有 API 接口统一返回此格式</p>
 *
 * @param <T> 数据类型
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    @Schema(description = "响应码", example = "200")
    private Integer code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳", example = "1700000000000")
    private Long timestamp;

    /**
     * 私有构造函数
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 私有构造函数
     *
     * @param code 响应码
     * @param message 响应消息
     * @param data 响应数据
     */
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success() {
        return new Result<>(BusinessCode.SUCCESS.getCode(), BusinessCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(BusinessCode.SUCCESS.getCode(), BusinessCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息和数据）
     *
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(BusinessCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应（使用错误码枚举）
     *
     * @param businessCode 错误码枚举
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(BusinessCode businessCode) {
        return new Result<>(businessCode.getCode(), businessCode.getMessage(), null);
    }

    /**
     * 失败响应（使用错误码枚举和自定义消息）
     *
     * @param businessCode 错误码枚举
     * @param message 自定义消息
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(BusinessCode businessCode, String message) {
        return new Result<>(businessCode.getCode(), message, null);
    }

    /**
     * 失败响应（使用错误码和消息）
     *
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（默认错误）
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(BusinessCode.INTERNAL_SERVER_ERROR.getCode(), message, null);
    }

    /**
     * 判断是否成功
     *
     * @return true=成功, false=失败
     */
    public boolean isSuccess() {
        return this.code != null && this.code.equals(BusinessCode.SUCCESS.getCode());
    }
}
