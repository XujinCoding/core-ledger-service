package com.coreledger.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API Request Logging Aspect
 * Logs all API requests with method, URI, parameters, response time, and status
 */
@Slf4j
@Aspect
@Component
public class ApiLogAspect {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 敏感字段列表（不打印这些字段的值）
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "password", "pwd", "secret", "token", "accessToken",
        "refreshToken", "apiKey", "privateKey", "credential"
    );

    // 最大参数长度（避免日志过长）
    private static final int MAX_PARAM_LENGTH = 500;

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("execution(public * com.coreledger.controller..*.*(..))")
    public void controllerMethods() {
    }

    /**
     * Around advice to log API requests
     */
    @Around("controllerMethods()")
    public Object logApiRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Get request information
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // Get method parameters
        String params = getMethodParameters(joinPoint);

        // Log request with parameters
        log.info("API Request: {} {} - {}.{}() - Params: {}",
            method, uri, className, methodName, params);

        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            // Execute the method
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Log response
            if (success) {
                log.info("API Response: {} {} - Status: SUCCESS, Time: {}ms",
                    method, uri, executionTime);
            } else {
                log.error("API Response: {} {} - Status: ERROR, Time: {}ms, Error: {}",
                    method, uri, executionTime, errorMessage);
            }

            // Warn if slow request (> 1 second)
            if (executionTime > 1000) {
                log.warn("Slow API detected: {} {} took {}ms", method, uri, executionTime);
            }
        }
    }

    /**
     * 获取方法参数
     */
    private String getMethodParameters(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Parameter[] parameters = signature.getMethod().getParameters();
            Object[] args = joinPoint.getArgs();

            if (args == null || args.length == 0) {
                return "{}";
            }

            Map<String, Object> paramMap = new LinkedHashMap<>();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];

                // 跳过不需要打印的参数类型
                if (shouldSkipParameter(arg)) {
                    continue;
                }

                String paramName = parameters[i].getName();
                Object paramValue = maskSensitiveData(paramName, arg);

                paramMap.put(paramName, paramValue);
            }

            String jsonString = objectMapper.writeValueAsString(paramMap);

            // 限制长度，避免日志过长
            if (jsonString.length() > MAX_PARAM_LENGTH) {
                return jsonString.substring(0, MAX_PARAM_LENGTH) + "... (truncated)";
            }

            return jsonString;

        } catch (Exception e) {
            log.warn("Failed to serialize method parameters: {}", e.getMessage());
            return "{error: 'Failed to serialize parameters'}";
        }
    }

    /**
     * 判断是否应该跳过该参数
     */
    private boolean shouldSkipParameter(Object arg) {
        if (arg == null) {
            return false;
        }

        // 跳过 Servlet 相关对象
        if (arg instanceof HttpServletRequest ||
            arg instanceof HttpServletResponse) {
            return true;
        }

        // 跳过文件上传对象
        if (arg instanceof MultipartFile) {
            return true;
        }

        return false;
    }

    /**
     * 屏蔽敏感数据
     */
    private Object maskSensitiveData(String paramName, Object paramValue) {
        if (paramValue == null) {
            return null;
        }

        // 检查参数名是否包含敏感字段
        String lowerParamName = paramName.toLowerCase();
        for (String sensitiveField : SENSITIVE_FIELDS) {
            if (lowerParamName.contains(sensitiveField.toLowerCase())) {
                return "******";
            }
        }

        // 如果是对象，尝试屏蔽对象内的敏感字段
        if (isComplexObject(paramValue)) {
            try {
                // 转换为 Map 进行处理
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.convertValue(paramValue, Map.class);
                return maskSensitiveFieldsInMap(map);
            } catch (Exception e) {
                // 如果转换失败，返回类名
                return paramValue.getClass().getSimpleName() + "@" + Integer.toHexString(paramValue.hashCode());
            }
        }

        return paramValue;
    }

    /**
     * 屏蔽 Map 中的敏感字段
     */
    private Map<String, Object> maskSensitiveFieldsInMap(Map<String, Object> map) {
        Map<String, Object> maskedMap = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 检查字段名是否敏感
            boolean isSensitive = SENSITIVE_FIELDS.stream()
                .anyMatch(field -> key.toLowerCase().contains(field.toLowerCase()));

            if (isSensitive) {
                maskedMap.put(key, "******");
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                maskedMap.put(key, maskSensitiveFieldsInMap(nestedMap));
            } else {
                maskedMap.put(key, value);
            }
        }

        return maskedMap;
    }

    /**
     * 判断是否为复杂对象（需要序列化）
     */
    private boolean isComplexObject(Object obj) {
        if (obj == null) {
            return false;
        }

        Class<?> clazz = obj.getClass();

        // 基本类型和包装类
        if (clazz.isPrimitive() ||
            obj instanceof String ||
            obj instanceof Number ||
            obj instanceof Boolean ||
            obj instanceof Character) {
            return false;
        }

        // 集合类型
        if (obj instanceof Collection || obj instanceof Map) {
            return false;
        }

        // 其他都认为是复杂对象
        return true;
    }
}
