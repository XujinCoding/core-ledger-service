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
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
    `wx_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_wx_openid` (`wx_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

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
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行政区划地址库';

-- =====================================================
-- 3. customer (客户表)
-- =====================================================
CREATE TABLE `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `alias` VARCHAR(50) DEFAULT NULL COMMENT '别名/昵称',
    `gender` VARCHAR(10) NOT NULL DEFAULT 'UNKNOWN' COMMENT '性别: MALE/FEMALE/UNKNOWN',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `address_id` BIGINT NOT NULL COMMENT '关联地址ID (必须是村级 level=5)',
    `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_address_id` (`address_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户信息表';

-- =====================================================
-- 4. product_category (商品分类表)
-- =====================================================
CREATE TABLE `product_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID (0表示顶级分类)',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- =====================================================
-- 5. product (商品表)
-- =====================================================
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '标准价格',
    `spec` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `unit` VARCHAR(20) NOT NULL DEFAULT '件' COMMENT '单位 (件/箱/斤等)',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品信息表';

-- =====================================================
-- 6. ledger (账本/订单主表) - 核心业务表
-- =====================================================
CREATE TABLE `ledger` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `total_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '应收总金额',
    `paid_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实收金额',
    `discount_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '抹零/优惠金额',
    `ledger_status` VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '账本状态: IN_PROGRESS/PARTIAL/CLEARED/CLOSED',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_ledger_status` (`ledger_status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本主表';

-- =====================================================
-- 7. ledger_item (账本明细表)
-- =====================================================
CREATE TABLE `ledger_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ledger_id` BIGINT NOT NULL COMMENT '账本ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称 (冗余存储)',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '实际售价 (可能与标准价不同)',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_ledger_id` (`ledger_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本明细表';

-- =====================================================
-- 8. payment_record (支付流水表)
-- =====================================================
CREATE TABLE `payment_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ledger_id` BIGINT NOT NULL COMMENT '账本ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式: WECHAT/CASH',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_ledger_id` (`ledger_id`),
    KEY `idx_payment_method` (`payment_method`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';

-- =====================================================
-- Initialize Default Admin User
-- Password: admin123 (BCrypt encrypted)
-- =====================================================
INSERT INTO `sys_user` (`username`, `password`, `phone`, `role`, `memo`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 'ADMIN', '系统默认管理员');
