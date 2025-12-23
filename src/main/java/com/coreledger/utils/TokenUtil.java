package com.coreledger.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.coreledger.vo.auth.CurrentUserIdentityInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Token 管理工具类
 * 基于 Redis 实现 Token 存储和管理
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Token 前缀
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
     * 生成 Token
     *
     * @param userInfo 用户信息
     * @return Token 字符串
     */
    public String generateToken(CurrentUserIdentityInfo userInfo) {
        // 生成唯一 Token
        String token = IdUtil.simpleUUID();

        // 存储到 Redis
        String key = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userInfo, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        log.info("生成Token成功: userId={}, token={}", userInfo.getUserId(), token);
        return token;
    }

    /**
     * 生成临时Token（用于多商户/多客户选择场景）
     * 有效期5分钟，仅允许访问切换身份等有限接口
     *
     * @param userInfo 用户信息（不含merchantId）
     * @return Token 字符串
     */
    public String generateTempToken(CurrentUserIdentityInfo userInfo) {
        String token = IdUtil.simpleUUID();
        String key = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userInfo, TEMP_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("生成临时Token成功: userId={}, token={}, expireMinutes={}", 
                userInfo.getUserId(), token, TEMP_TOKEN_EXPIRE_MINUTES);
        return token;
    }

    /**
     * 获取临时Token过期时间
     */
    public LocalDateTime getTempExpireTime() {
        return LocalDateTime.now().plusMinutes(TEMP_TOKEN_EXPIRE_MINUTES);
    }

    /**
     * 验证 Token 并获取用户信息
     *
     * @param token Token 字符串
     * @return 用户信息，Token 无效时返回 null
     */
    public CurrentUserIdentityInfo getCurrentUserIdentityInfo(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }

        String key = TOKEN_PREFIX + token;
        Object obj = redisTemplate.opsForValue().get(key);

        if (obj instanceof CurrentUserIdentityInfo) {
            // 刷新过期时间
            redisTemplate.expire(key, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
            return (CurrentUserIdentityInfo) obj;
        }

        return null;
    }

    /**
     * 删除 Token（登出）
     *
     * @param token Token 字符串
     */
    public void removeToken(String token) {
        if (StrUtil.isBlank(token)) {
            return;
        }

        String key = TOKEN_PREFIX + token;
        redisTemplate.delete(key);
        log.info("删除Token成功: token={}", token);
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
