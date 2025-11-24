package com.coreledger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatConfig {

    /**
     * 小程序AppID
     */
    private String appid;

    /**
     * 小程序AppSecret
     */
    private String secret;

    /**
     * 微信API域名
     */
    private String apiDomain = "https://api.weixin.qq.com";

    /**
     * code2Session接口地址
     */
    public String getCode2SessionUrl() {
        return apiDomain + "/sns/jscode2session";
    }
}
