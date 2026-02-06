package com.coreledger.utils;

import cn.hutool.core.util.StrUtil;
import com.coreledger.security.JwtTokenProvider;
import com.coreledger.vo.auth.CurrentUserIdentityInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Token 管理工具类
 * 使用 JWT 生成 Token，同时将 Token 和用户信息存储在 Redis 中
 * Token 作为 Redis 的 key，用户信息作为 value
 * 登出时直接删除 Redis 中的 Token
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Token 存储前缀
     */
    private static final String TOKEN_PREFIX = "token:";

    /**
     * Token 有效期（天）
     */
    private static final int TOKEN_EXPIRE_DAYS = 7;

    /**
     * 临时Token有效期（分钟）
     */
    private static final int TEMP_TOKEN_EXPIRE_MINUTES = 5;

    /**
     * 生成 JWT Token 并存储到 Redis
     *
     * @param userInfo 用户信息
     * @return JWT Token 字符串
     */
    public String generateToken(CurrentUserIdentityInfo userInfo) {
        // 使用 JwtTokenProvider 生成 JWT Token
        JwtTokenProvider.TokenResult tokenResult = jwtTokenProvider.generateToken(
                userInfo.getUserId(),
                userInfo.getMerchantId(),
                userInfo.getIdentityType()
        );

        String token = tokenResult.token();

        // 将 JWT Token 作为 key，用户信息作为 value 存储到 Redis
        String key = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userInfo, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        log.info("生成JWT Token并存储到Redis成功: userId={}, merchantId={}, identityType={}",
                userInfo.getUserId(), userInfo.getMerchantId(), userInfo.getIdentityType());
        return token;
    }

    /**
     * 生成临时 JWT Token（用于多商户/多客户选择场景）
     * 有效期5分钟，仅允许访问切换身份等有限接口
     *
     * @param userInfo 用户信息（不含merchantId）
     * @return JWT Token 字符串
     */
    public String generateTempToken(CurrentUserIdentityInfo userInfo) {
        // 使用 JwtTokenProvider 生成临时 JWT Token
        JwtTokenProvider.TokenResult tokenResult = jwtTokenProvider.generateTempToken(userInfo.getUserId());

        String token = tokenResult.token();

        // 将 JWT Token 作为 key，用户信息作为 value 存储到 Redis，有效期5分钟
        String key = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userInfo, TEMP_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("生成临时JWT Token并存储到Redis成功: userId={}, expireMinutes={}",
                userInfo.getUserId(), TEMP_TOKEN_EXPIRE_MINUTES);
        return token;
    }

    /**
     * 获取临时Token过期时间
     */
    public LocalDateTime getTempExpireTime() {
        return LocalDateTime.now().plusMinutes(TEMP_TOKEN_EXPIRE_MINUTES);
    }

    /**
     * 从 Redis 获取用户信息
     *
     * @param token Token 字符串
     * @return 用户信息，Token 无效时返回 null
     */
    public CurrentUserIdentityInfo getCurrentUserIdentityInfo(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }

        // 从 Redis 中获取用户信息
        try {
            String key = TOKEN_PREFIX + token;
            Object value = redisTemplate.opsForValue().get(key);

            if (value instanceof CurrentUserIdentityInfo) {
                return (CurrentUserIdentityInfo) value;
            }

            log.warn("Token不存在或已过期: token={}", token);
            return null;
        } catch (Exception e) {
            log.error("从Redis获取用户信息失败: token={}, error={}", token, e.getMessage());
            return null;
        }
    }

    /**
     * 删除 Token（登出）
     * 直接从 Redis 中删除 Token
     *
     * @param token Token 字符串
     */
    public void removeToken(String token) {
        if (StrUtil.isBlank(token)) {
            return;
        }

        // 直接从 Redis 中删除 Token
        String key = TOKEN_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Token已从Redis删除: token={}", token);
    }

    /**
     * 获取 Token 过期时间
     *
     * @return 过期时间
     */
    public LocalDateTime getExpireTime() {
        return LocalDateTime.now().plusDays(TOKEN_EXPIRE_DAYS);
    }

    /**
     * 检查 Token 是否有效
     *
     * @param token Token 字符串
     * @return true=有效, false=无效
     */
    public boolean isValid(String token) {
        return getCurrentUserIdentityInfo(token) != null;
    }
}
