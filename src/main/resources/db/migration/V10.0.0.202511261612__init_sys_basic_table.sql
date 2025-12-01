-- =====================================================
-- Core Ledger System - Initial Database Schema
-- Version: 1.0.0
-- Author: Core Ledger Team
-- Description: 核芯账本系统初始化数据库表结构
-- =====================================================

-- =====================================================
-- 1. sys_user (用户表)
-- =====================================================
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名（可选，管理员必填）',
    `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(BCrypt加密，管理员必填)',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0=普通用户, 1=管理员',
    `wx_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID（唯一标识）',
    `wx_nickname` VARCHAR(100) DEFAULT NULL COMMENT '微信昵称',
    `wx_avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '微信头像URL',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_wx_openid` (`wx_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 2. sys_address (地址库/行政区划表)
-- =====================================================
CREATE TABLE `sys_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID (0表示顶级)',
    `name` VARCHAR(100) NOT NULL COMMENT '地址名称',
    `level` TINYINT NOT NULL COMMENT '层级: 1=省, 2=市, 3=区县, 4=镇/乡, 5=村',
    `merger_name` VARCHAR(500) DEFAULT NULL COMMENT '全称路径 (如: 广东省-深圳市-南山区-西丽街道-留仙村)',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行政区划地址库' AUTO_INCREMENT = 10000;

-- =====================================================
-- Initialize Default Admin User
-- Password: admin123 (BCrypt encrypted)
-- Role: 1 (管理员)
-- =====================================================
INSERT INTO `sys_user` (`username`, `password`, `phone`, `role`, `memo`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 1, '系统默认管理员');
