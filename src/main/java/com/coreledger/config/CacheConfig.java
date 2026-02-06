package com.coreledger.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置
 * 配置 Spring Cache 使用 Redis 作为缓存存储
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存名称常量
     */
    public static final String CACHE_MERCHANTS = "merchants";
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_CUSTOMERS = "customers";
    public static final String CACHE_USERS = "users";
    public static final String CACHE_PRODUCT_CATEGORIES = "productCategories";

    /**
     * 配置 Redis 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建 ObjectMapper 用于序列化
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册 JavaTimeModule 以支持 Java 8 日期时间类型
        objectMapper.registerModule(new JavaTimeModule());

        // 激活默认类型信息
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 配置序列化器
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 默认缓存配置: 1小时过期
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // 为不同的缓存配置不同的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 商户信息: 2小时
        cacheConfigurations.put(CACHE_MERCHANTS, defaultConfig.entryTtl(Duration.ofHours(2)));

        // 产品信息: 1小时
        cacheConfigurations.put(CACHE_PRODUCTS, defaultConfig.entryTtl(Duration.ofHours(1)));

        // 客户信息: 30分钟
        cacheConfigurations.put(CACHE_CUSTOMERS, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // 用户信息: 1小时
        cacheConfigurations.put(CACHE_USERS, defaultConfig.entryTtl(Duration.ofHours(1)));

        // 产品分类: 24小时 (很少变化)
        cacheConfigurations.put(CACHE_PRODUCT_CATEGORIES, defaultConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
