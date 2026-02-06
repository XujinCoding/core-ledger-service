package com.coreledger.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAuthorizationService authz;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (使用 JWT 不需要 CSRF 保护)
                .csrf(AbstractHttpConfigurer::disable)

                // 配置安全响应头
                .headers(headers -> headers
                        // 防止点击劫持攻击
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        // 防止 MIME 类型嗅探
                        .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable)
                        // XSS 保护
                        .xssProtection(HeadersConfigurer.XXssConfig::disable) // 现代浏览器已内置,不需要此头
                        // 内容安全策略
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
                        )
                )

                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 配置会话管理 (无状态)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 (无需认证)
                        .requestMatchers(
                                "/api/auth/wechat-login",              // 微信登录
                                "/api/addresses/**",                   // 地址接口
                                "/api/auth/merchant/wechat/register",  // 商户注册
                                "/api/auth/customer/wechat/register",  // 客户注册
                                "/api/sms/send",                       // 短信发送
                                "/api/auth/register",                  // 注册
                                "/api/auth/login",                     // 登录
                                "/api/auth/send-sms-code",             // 发送短信验证码
                                "/health",                             // 健康检查
                                "/actuator/**",                        // Actuator 端点
                                "/doc.html",                           // Knife4j 文档
                                "/swagger-ui/**",                      // Swagger UI
                                "/v3/api-docs/**",                     // OpenAPI 文档
                                "/webjars/**"                          // Webjars 资源
                        ).permitAll()

                        // 临时Token可访问的接口（未选择身份时）
                        .requestMatchers(
                                "/api/auth/switch-identity",           // 切换身份
                                "/api/auth/identities",                // 获取用户身份列表
                                "/api/auth/logout",                    // 登出
                                "/api/auth/current-user"               // 获取当前用户信息
                        ).authenticated()

                        // 其他所有请求需要已选择身份
                        .anyRequest().access((authentication, context) ->
                                new AuthorizationDecision(authz.hasSelectedIdentity())
                        )
                )

                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

