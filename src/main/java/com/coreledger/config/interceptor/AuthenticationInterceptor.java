package com.coreledger.config.interceptor;

import com.coreledger.enums.IdentityType;
import com.coreledger.utils.AppSessionContext;
import com.coreledger.utils.TokenUtil;
import com.coreledger.vo.auth.UserInfoVO;
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
    private final EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取 Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            // 不需要认证的接口直接放行
            return true;
        }

        try {
            // 2. 解析 Token 获取用户信息
            UserInfoVO userInfo = tokenUtil.getUserInfo(token);
            if (userInfo == null) {
                return true;
            }

            // 3. 构建会话信息对象
            IdentityType identityType = userInfo.getIdentityType() != null ? userInfo.getIdentityType() : null;
            SessionInfo sessionInfo = SessionInfo.builder()
                    .userId(userInfo.getId())
                    .username(userInfo.getUsername())
                    .merchantId(userInfo.getMerchantId())
                    .merchantName(userInfo.getMerchantName())
                    .merchantNo(userInfo.getMerchantNo())
                    .customerId(userInfo.getCustomerId())
                    .customerName(userInfo.getCustomerName())
                    .customerNo(userInfo.getCustomerNo())
                    .customerPhone(userInfo.getCustomerPhone())
                    .identityType(identityType)
                    .build();

            // 4. 设置 AppSessionContext
            AppSessionContext.setSessionInfo(sessionInfo);

            // 5. 启用 Hibernate Filter
            if (userInfo.getMerchantId() != null) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("merchantFilter")
                        .setParameter("merchantId", userInfo.getMerchantId());
                log.debug("启用 merchantFilter: merchantId={}", userInfo.getMerchantId());
            }

            log.debug("认证成功: userId={}, username={}, merchantId={}, customerId={}, identityType={}", 
                    userInfo.getId(), userInfo.getUsername(), userInfo.getMerchantId(), 
                    userInfo.getCustomerId(), userInfo.getIdentityTypeValue());

        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            // Token 解析失败不影响请求继续
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空 AppSessionContext
        AppSessionContext.clear();
    }
}
