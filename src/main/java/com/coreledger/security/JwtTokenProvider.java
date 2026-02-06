package com.coreledger.security;

import com.coreledger.enums.IdentityType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token 提供者
 * 负责生成、验证和解析 JWT Token
 * <p>
 * 设计说明：
 * - JWT 只存储基本信息（userId, merchantId, identityType, jti）
 * - 详细的会话信息存储在 Redis 中，使用 jti 作为 key
 * - 这样既保持了 JWT 的轻量和安全性，又能存储更多信息
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:core-ledger-secret-key-change-this-in-production-environment-minimum-256-bits}")
    private String jwtSecret;

    @Value("${jwt.expiration:604800000}") // 7 days in milliseconds
    private long jwtExpiration;

    @Value("${jwt.temp-expiration:300000}") // 5 minutes in milliseconds
    private long jwtTempExpiration;

    /**
     * 生成 JWT Token
     *
     * @param userId       用户ID
     * @param merchantId   商户ID
     * @param identityType 身份类型
     * @return JWT Token 和 jti
     */
    public TokenResult generateToken(Long userId, Long merchantId, IdentityType identityType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // 生成唯一的 JWT ID
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("merchantId", merchantId)
                .claim("identityType", identityType != null ? identityType.name() : null)
                .id(jti)  // 添加 JWT ID
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        return new TokenResult(token, jti, expiryDate.getTime());
    }

    /**
     * 生成临时 JWT Token (用于多商户/多客户选择场景)
     *
     * @param userId 用户ID
     * @return JWT Token 和 jti
     */
    public TokenResult generateTempToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtTempExpiration);

        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("temp", true)
                .id(jti)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        return new TokenResult(token, jti, expiryDate.getTime());
    }

    /**
     * 验证 Token 是否有效（签名和过期时间）
     *
     * @param token JWT Token
     * @return true=有效, false=无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已过期: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT Token 无效: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
         * Token 生成结果
         */
        public record TokenResult(String token, String jti, long expirationTime) {
    }
}
