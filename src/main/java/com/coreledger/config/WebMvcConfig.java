package com.coreledger.config;

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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
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
