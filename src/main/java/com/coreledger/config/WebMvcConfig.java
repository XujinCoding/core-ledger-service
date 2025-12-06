package com.coreledger.config;

import com.coreledger.config.interceptor.AuthenticationInterceptor;
import com.coreledger.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器 - 用于解析 Token、设置 AppSessionContext、启用 Hibernate Filter
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有 API 请求
                .excludePathPatterns(
                        "/api/auth/wechat-login",    // 排除微信登录
                        "/api/auth/register",        // 排除注册
                        "/api/auth/login",           // 排除登录
                        "/api/doc.html",             // 排除 Knife4j 文档
                        "/api/swagger-ui/**",
                        "/api/v3/api-docs/**"
                );

        // 原有的认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有 API 请求
                .excludePathPatterns(
                        "/api/auth/**",      // 排除认证接口
                        "/api/doc.html",     // 排除 Knife4j 文档
                        "/api/swagger-ui/**",
                        "/api/v3/api-docs/**"
                );
    }
}
