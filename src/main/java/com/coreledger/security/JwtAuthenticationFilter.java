package com.coreledger.security;

import cn.hutool.core.util.StrUtil;
import com.coreledger.common.mapper.sysuser.SysUserConvert;
import com.coreledger.enums.IdentityType;
import com.coreledger.utils.TokenUtil;
import com.coreledger.vo.auth.CurrentUserIdentityInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从请求头中提取 Token，校验 JWT 签名，从 Redis 获取用户信息并设置 Spring Security 上下文
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenUtil tokenUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserConvert sysUserConvert;

    private static final String HEADER_TOKEN = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractTokenFromRequest(request);

            if (StrUtil.isNotBlank(token)) {
                // 1. 校验 JWT 签名是否有效
                if (jwtTokenProvider.validateToken(token)) {
                    // 2. 从 Redis 中获取用户信息
                    CurrentUserIdentityInfo userInfo = tokenUtil.getCurrentUserIdentityInfo(token);

                    if (userInfo == null) {
                        log.warn("Token 在 Redis 中不存在或已过期: uri={}", request.getRequestURI());
                        // Token 不在 Redis 中（可能已登出或过期），不设置认证信息
                    } else {
                        // 3. 构建 UserPrincipal
                        UserPrincipal principal = sysUserConvert.toPrincipal(userInfo);
                        principal.setToken(token);

                        // 4. 设置 Spring Security 上下文
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,
                                        principal.getAuthorities()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Token 认证成功: userId={}, merchantId={}, customerId={}, identityType={}, uri={}",
                                principal.getUserId(), principal.getMerchantId(),
                                principal.getCustomerId(), principal.getIdentityType(), request.getRequestURI());
                    }
                } else {
                    log.warn("JWT Token 签名无效: uri={}", request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.error("Token 认证失败: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Token
     *
     * @param request HTTP 请求
     * @return Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_TOKEN);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
