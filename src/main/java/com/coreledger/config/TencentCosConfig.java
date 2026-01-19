package com.coreledger.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tencent.cos")
public class TencentCosConfig {

    /**
     * SecretId
     */
    private String secretId;

    /**
     * SecretKey
     */
    private String secretKey;

    /**
     * 存储桶名称 (格式: bucketName-appId)
     */
    private String bucketName;

    /**
     * 地域 (如: ap-guangzhou, ap-shanghai, ap-beijing)
     */
    private String region;

    /**
     * 存储路径前缀
     */
    private String pathPrefix = "images/";

    /**
     * 自定义域名 (可选，用于 CDN 加速)
     */
    private String customDomain;

    /**
     * 创建 COS 客户端
     */
    @Bean
    public COSClient cosClient() {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(credentials, clientConfig);
    }
}
