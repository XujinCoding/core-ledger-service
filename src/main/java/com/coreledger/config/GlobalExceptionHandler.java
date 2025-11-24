package com.coreledger.config;

import com.coreledger.common.Result;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.ForbiddenException;
import com.coreledger.exception.NotFoundException;
import com.coreledger.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>统一处理系统中的各类异常，返回标准的错误响应格式</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理资源不存在异常
     *
     * @param e 资源不存在异常
     * @return 错误响应
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNotFoundException(NotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理未授权异常
     *
     * @param e 未授权异常
     * @return 错误响应
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("未授权访问: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理无权限异常
     *
     * @param e 无权限异常
     * @return 错误响应
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleForbiddenException(ForbiddenException e) {
        log.warn("无权限访问: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 注解）
     *
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMessage);
        return Result.error(BusinessCode.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e 参数绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", errorMessage);
        return Result.error(BusinessCode.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理约束违反异常（@Validated 注解）
     *
     * @param e 约束违反异常
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", errorMessage);
        return Result.error(BusinessCode.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e 缺少请求参数异常
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String errorMessage = String.format("缺少必需参数: %s", e.getParameterName());
        log.warn(errorMessage);
        return Result.error(BusinessCode.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e 参数类型不匹配异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String errorMessage = String.format("参数类型错误: %s", e.getName());
        log.warn(errorMessage);
        return Result.error(BusinessCode.BAD_REQUEST, errorMessage);
    }

    /**
     * 处理消息不可读异常（JSON 格式错误）
     *
     * @param e 消息不可读异常
     * @return 错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return Result.error(BusinessCode.BAD_REQUEST, "请求体格式错误");
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e 请求方法不支持异常
     * @return 错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String errorMessage = String.format("不支持的请求方法: %s", e.getMethod());
        log.warn(errorMessage);
        return Result.error(405, errorMessage);
    }

    /**
     * 处理路径不存在异常
     *
     * @param e 路径不存在异常
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        String errorMessage = String.format("请求路径不存在: %s", e.getRequestURL());
        log.warn(errorMessage);
        return Result.error(BusinessCode.NOT_FOUND, errorMessage);
    }

    /**
     * 处理乐观锁冲突异常
     *
     * @param e 乐观锁冲突异常
     * @return 错误响应
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<?> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e) {
        log.warn("乐观锁冲突: {}", e.getMessage());
        return Result.error(BusinessCode.OPTIMISTIC_LOCK_CONFLICT);
    }

    /**
     * 处理数据完整性违反异常（唯一约束、外键约束等）
     *
     * @param e 数据完整性违反异常
     * @return 错误响应
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据完整性违反: {}", e.getMessage());
        String errorMessage = "数据操作失败，可能违反了唯一性约束或外键约束";
        if (e.getMessage() != null) {
            if (e.getMessage().contains("Duplicate entry")) {
                errorMessage = "数据已存在，请勿重复添加";
            } else if (e.getMessage().contains("foreign key constraint")) {
                errorMessage = "数据关联错误，请检查关联数据是否存在";
            }
        }
        return Result.error(BusinessCode.DATABASE_ERROR, errorMessage);
    }

    /**
     * 处理所有未捕获的异常
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(BusinessCode.INTERNAL_SERVER_ERROR);
    }
}
