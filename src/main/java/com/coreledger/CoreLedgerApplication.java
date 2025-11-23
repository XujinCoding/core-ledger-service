package com.coreledger;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Core Ledger Application Main Entry
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.coreledger.repository")
@MapperScan("com.coreledger.mapper")
@EnableTransactionManagement
public class CoreLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreLedgerApplication.class, args);
    }
}
