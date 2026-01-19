package com.coreledger.interceptor;

import cn.hutool.core.util.StrUtil;
import com.coreledger.enums.BusinessCode;
import com.coreledger.enums.IdentityType;
import com.coreledger.exception.BusinessException;
import com.coreledger.exception.UnauthorizedException;
import com.coreledger.utils.AppSessionContext;
import com.coreledger.utils.TokenUtil;
import com.coreledger.vo.auth.CurrentUserIdentityInfo;
import com.coreledger.vo.session.SessionInfo;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 用于从 Token 中提取用户信息，设置 AppSessionContext，启用 Hibernate Filter
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final TokenUtil tokenUtil;

    /**
     * 请求头中 Token 的 key
     */
    private static final String HEADER_TOKEN = "Authorization";

    /**
     * Token 前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    private final EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取 Token
        String authHeader = request.getHeader(HEADER_TOKEN);

        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.warn("请求未携带Token: uri={}", request.getRequestURI());
            throw new UnauthorizedException();
        }

        // 提取 Token
        String token = authHeader.substring(TOKEN_PREFIX.length());
        CurrentUserIdentityInfo userInfo = tokenUtil.getCurrentUserIdentityInfo(token);

        // 2. 解析 Token 获取用户信息
        if (userInfo == null) {
            log.warn("Token无效或已过期: token={}, uri={}", token, request.getRequestURI());
            throw new UnauthorizedException();
        }

        log.debug("用户认证成功: userId={}, uri={}, username={}, merchantId={}, identityType={}",
                userInfo.getUserId(), request.getRequestURI(),userInfo.getName(), userInfo.getMerchantId(), userInfo.getIdentityType());
        // 3. 构建会话信息对象
        SessionInfo.SessionInfoBuilder sessionInfoBuilder = SessionInfo.builder()
                .userId(userInfo.getUserId())
                .token(token)
                .merchantId(userInfo.getMerchantId())
                .identityType(IdentityType.MERCHANT_OWNER);
        SessionInfo sessionInfo = sessionInfoBuilder.build();
        if (IdentityType.CUSTOMER.equals(userInfo.getIdentityType())) {
            sessionInfo.setCustomerId(userInfo.getId());
            sessionInfo.setIdentityType(IdentityType.CUSTOMER);
        }

        // 4. 设置 AppSessionContext
        AppSessionContext.setSessionInfo(sessionInfo);

        // 5. 检查商户身份是否已选择商户（临时token限制）
        if (IdentityType.MERCHANT_OWNER.equals(userInfo.getIdentityType()) 
                && userInfo.getMerchantId() == null) {
            String uri = request.getRequestURI();
            if (!isAllowedWithoutIdentity(uri)) {
                log.warn("商户身份未选择商户: userId={}, uri={}", userInfo.getUserId(), uri);
                throw new BusinessException(BusinessCode.MERCHANT_NOT_SELECTED);
            }
        }

        // 7. 检查客户身份是否已选择客户（临时token限制）
        if (IdentityType.CUSTOMER.equals(userInfo.getIdentityType()) 
                && userInfo.getMerchantId() == null && userInfo.getId() == null) {
            String uri = request.getRequestURI();
            if (!isAllowedWithoutIdentity(uri)) {
                log.warn("客户身份未选择客户: userId={}, uri={}", userInfo.getUserId(), uri);
                throw new BusinessException(BusinessCode.MERCHANT_NOT_SELECTED);
            }
        }

        return true;
    }

    /**
     * 检查是否允许未选择身份时访问的接口（临时token白名单）
     */
    private boolean isAllowedWithoutIdentity(String uri) {
        return uri.contains("/auth/switch-identity")
                || uri.contains("/auth/identities")
                || uri.contains("/auth/logout")
                || uri.contains("/auth/current-user");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空 AppSessionContext
        AppSessionContext.clear();
    }
}
