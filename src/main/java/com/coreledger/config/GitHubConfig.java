package com.coreledger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * GitHub 图床配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "github")
public class GitHubConfig {

    /**
     * GitHub 账号名
     */
    private String owner;

    /**
     * 仓库名
     */
    private String repo;

    /**
     * 分支名
     */
    private String branch = "main";

    /**
     * Personal Access Token
     */
    private String token;

    /**
     * 存储路径
     */
    private String path = "";

    /**
     * 提交者名称
     */
    private String name;

    /**
     * 提交者邮箱
     */
    private String email;
}
