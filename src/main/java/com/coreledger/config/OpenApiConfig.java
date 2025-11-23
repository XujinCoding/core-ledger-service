package com.coreledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j/OpenAPI Configuration
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Core Ledger API Documentation")
                        .version("1.0.0")
                        .description("核芯账本系统 RESTful API 接口文档")
                        .contact(new Contact()
                                .name("Core Ledger Team")
                                .email("support@coreledger.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
