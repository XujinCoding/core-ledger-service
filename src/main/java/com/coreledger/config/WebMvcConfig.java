package com.coreledger.config;

import com.coreledger.config.converter.StringToBaseEnumConverterFactory;
import com.coreledger.interceptor.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
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

    private final AuthenticationInterceptor authenticationInterceptor;
    private final StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(stringToBaseEnumConverterFactory);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器 - 用于解析 Token、设置 AppSessionContext、启用 Hibernate Filter
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有 API 请求
                .excludePathPatterns(
                        "/api/auth/wechat-login",    // 排除微信登录
                        "/api/addresses/**",    // 排除微信登录
                        "/api/auth/merchant/wechat/register",    // 排除商户注册
                        "/api/auth/customer/wechat/register",    // 排除客户注册
                        "/api/sms/send",    // 排除客户注册
                        "/api/auth/register",        // 排除注册
                        "/api/auth/login",           // 排除登录
                        "/api/doc.html",             // 排除 Knife4j 文档
                        "/api/swagger-ui/**",
                        "/api/v3/api-docs/**"
                );
    }
}
