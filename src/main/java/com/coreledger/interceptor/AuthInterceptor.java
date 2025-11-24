package com.coreledger.interceptor;

import cn.hutool.core.util.StrUtil;
import com.coreledger.enums.BusinessCode;
import com.coreledger.exception.UnauthorizedException;
import com.coreledger.utils.TokenUtil;
import com.coreledger.vo.auth.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 拦截需要登录的接口，验证 Token 有效性
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenUtil tokenUtil;

    /**
     * 请求头中 Token 的 key
     */
    private static final String HEADER_TOKEN = "Authorization";

    /**
     * Token 前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * ThreadLocal 存储当前用户信息
     */
    private static final ThreadLocal<UserInfoVO> USER_CONTEXT = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取 Token
        String authHeader = request.getHeader(HEADER_TOKEN);

        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.warn("请求未携带Token: uri={}", request.getRequestURI());
            throw new UnauthorizedException();
        }

        // 提取 Token
        String token = authHeader.substring(TOKEN_PREFIX.length());

        // 验证 Token 并获取用户信息
        UserInfoVO userInfo = tokenUtil.getUserInfo(token);

        if (userInfo == null) {
            log.warn("Token无效或已过期: token={}, uri={}", token, request.getRequestURI());
            throw new UnauthorizedException();
        }

        // 存储到 ThreadLocal
        USER_CONTEXT.set(userInfo);
        log.debug("用户认证成功: userId={}, uri={}", userInfo.getId(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 ThreadLocal
        USER_CONTEXT.remove();
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    public static UserInfoVO getCurrentUser() {
        return USER_CONTEXT.get();
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        UserInfoVO userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getId() : null;
    }
}
