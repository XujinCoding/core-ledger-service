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
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
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
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别: 0=未知, 1=男, 2=女',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `address_id` BIGINT NOT NULL COMMENT '关联地址ID (必须是村级 level=5)',
    `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
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
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
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
    `unit` VARCHAR(20) NOT NULL DEFAULT '件' COMMENT '单位 (件/箱/斤/公斤等)',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品信息表';

-- =====================================================
-- 6. ledger (账本/订单主表) - 核心业务表
-- 状态说明:
--   1 = IN_PROGRESS (进行中): 账单创建，未收款
--   2 = PARTIAL (部分缴费): 已收部分款项，继续收款
--   3 = CLEARED (已结清): 完全缴费或抹零结清
--   4 = ON_CREDIT (赊账中): 客户赊账，暂不催收
--   5 = CLOSED (已关闭): 作废的账单
--
-- 业务规则:
--   - 主页面查询: 只显示状态为 1 (IN_PROGRESS) 和 2 (PARTIAL) 的账单
--   - 客户详情页: 显示该客户所有状态的账单（包括赊账和已结清）
--   - 状态转换详见: docs/LEDGER_STATUS_TRANSITIONS.md
-- =====================================================
CREATE TABLE `ledger` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `total_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '应收总金额',
    `paid_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实收金额',
    `discount_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '抹零/优惠金额',
    `ledger_status` TINYINT NOT NULL DEFAULT 1 COMMENT '账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_ledger_status` (`ledger_status`),
    KEY `idx_create_instant` (`create_instant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本主表';

-- =====================================================
-- 7. ledger_item (账本明细表)
-- 业务规则:
--   - status 字段用于软删除: 1=有效, 0=已删除
--   - 支持创建后修改明细（增删改商品）
-- =====================================================
CREATE TABLE `ledger_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ledger_id` BIGINT NOT NULL COMMENT '账本ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称 (冗余存储)',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '实际售价 (可能与标准价不同)',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=已删除 (软删除)',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_ledger_id` (`ledger_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本明细表';

-- =====================================================
-- 8. payment_record (支付流水表)
-- 业务规则:
--   - status 字段用于软删除: 1=有效, 0=已删除
--   - 支持删除支付记录（软删除）
-- =====================================================
CREATE TABLE `payment_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ledger_id` BIGINT NOT NULL COMMENT '账本ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_method` TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式: 1=现金, 2=微信, 3=支付宝, 4=银行转账',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=已删除 (软删除)',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_ledger_id` (`ledger_id`),
    KEY `idx_payment_method` (`payment_method`),
    KEY `idx_create_instant` (`create_instant`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';

-- =====================================================
-- Initialize Default Admin User
-- Password: admin123 (BCrypt encrypted)
-- Role: 1 (管理员)
-- =====================================================
INSERT INTO `sys_user` (`username`, `password`, `phone`, `role`, `memo`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 1, '系统默认管理员');
