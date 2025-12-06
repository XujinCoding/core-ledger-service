-- =====================================================
-- Core Ledger System - Authentication Module Redesign
-- Version: 1.0.0
-- Author: Core Ledger Team
-- Date: 2025-12-06
-- Description: 登录认证模块重设计 - 商户体系、数据隔离、身份管理
-- =====================================================

-- =====================================================
-- 0. sys_user (系统用户表) - 修改
-- 新增字段: identity_type, binding_id
-- =====================================================
ALTER TABLE `sys_user`
    ADD COLUMN `identity_type` TINYINT DEFAULT NULL COMMENT '身份类型: 1=商户所有者, 2=客户' AFTER `role`,
    ADD COLUMN `binding_id` BIGINT DEFAULT NULL COMMENT '绑定ID: 根据identity_type关联到Merchant.id或Customer.id' AFTER `identity_type`;

-- =====================================================
-- 1. merchant (商户表) - 新建
-- =====================================================
CREATE TABLE `merchant` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_no`    VARCHAR(32)  NOT NULL COMMENT '商户编号，格式：M_yyyyMMddHHmmss_随机3位',
    `merchant_name`  VARCHAR(100) NOT NULL COMMENT '商户名称',
    `owner_user_id`  BIGINT       NOT NULL COMMENT '商户所有者User ID',
    `invite_code`    VARCHAR(20)  NOT NULL COMMENT '邀请码，用于生成二维码',
    `qr_code_url`    VARCHAR(500) DEFAULT NULL COMMENT '二维码URL',
    `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `memo`           VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_no` (`merchant_no`),
    UNIQUE KEY `uk_invite_code` (`invite_code`),
    KEY `idx_owner_user_id` (`owner_user_id`),
    CONSTRAINT `fk_merchant_owner_user_id` FOREIGN KEY (`owner_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '商户表'
  AUTO_INCREMENT = 10000;

-- =====================================================
-- 2. customer (客户信息表) - 修改
-- 新增字段: customer_no, user_id, merchant_id, is_registered
-- =====================================================
-- 先删除现有的 uk_phone 唯一约束，因为同一个手机号可以在多个商户下有不同的客户记录
ALTER TABLE `customer`
    DROP KEY `uk_phone`;

-- 添加新字段
ALTER TABLE `customer`
    ADD COLUMN `customer_no` VARCHAR(32) NOT NULL COMMENT '客户编号，格式：C_yyyyMMddHHmmss_随机3位' AFTER `id`,
    ADD COLUMN `user_id` BIGINT DEFAULT NULL COMMENT '关联的User ID，允许为空（商户手动创建时）' AFTER `customer_no`,
    ADD COLUMN `merchant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '所属商户ID' AFTER `user_id`,
    ADD COLUMN `is_registered` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已注册: 0=未注册, 1=已注册' AFTER `merchant_id`;

-- 为 customer 表添加唯一约束和索引
ALTER TABLE `customer`
    ADD UNIQUE KEY `uk_user_merchant` (`user_id`, `merchant_id`),
    ADD UNIQUE KEY `uk_customer_no_merchant` (`customer_no`, `merchant_id`),
    ADD KEY `idx_merchant_id` (`merchant_id`),
    ADD KEY `idx_user_id` (`user_id`),
    ADD KEY `idx_phone` (`phone`);

-- 为 customer 表添加外键约束
ALTER TABLE `customer`
    ADD CONSTRAINT `fk_customer_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
    ADD CONSTRAINT `fk_customer_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`);

-- =====================================================
-- 3. ledger (账本主表) - 修改
-- 新增字段: merchant_id
-- =====================================================
ALTER TABLE `ledger`
    ADD COLUMN `merchant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '所属商户ID，用于数据隔离' AFTER `id`;

-- 为 ledger 表添加索引和外键
ALTER TABLE `ledger`
    ADD KEY `idx_merchant_id` (`merchant_id`),
    ADD CONSTRAINT `fk_ledger_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`);

-- =====================================================
-- 4. product (商品信息表) - 修改
-- 新增字段: merchant_id
-- =====================================================
ALTER TABLE `product`
    ADD COLUMN `merchant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '所属商户ID，用于数据隔离' AFTER `id`;

-- 为 product 表添加索引和外键
ALTER TABLE `product`
    ADD KEY `idx_merchant_id` (`merchant_id`),
    ADD CONSTRAINT `fk_product_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`);

-- =====================================================
-- 5. product_category (商品分类表) - 修改
-- 新增字段: merchant_id
-- =====================================================
ALTER TABLE `product_category`
    ADD COLUMN `merchant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '所属商户ID，用于数据隔离' AFTER `id`;

-- 为 product_category 表添加索引和外键
ALTER TABLE `product_category`
    ADD KEY `idx_merchant_id` (`merchant_id`),
    ADD CONSTRAINT `fk_product_category_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`);

-- =====================================================
-- 6. 为现有数据迁移 merchant_id（如果存在数据）
-- =====================================================
-- 为 customer 表的现有记录设置默认 merchant_id
-- 注意：如果 customer 表中已有数据，需要根据实际业务逻辑进行迁移
-- 这里使用占位符 merchant_id = 1，实际迁移时需要调整
-- UPDATE `customer` SET `merchant_id` = 1 WHERE `merchant_id` IS NULL;

-- 为 ledger 表的现有记录设置默认 merchant_id
-- UPDATE `ledger` SET `merchant_id` = 1 WHERE `merchant_id` IS NULL;

-- 为 product 表的现有记录设置默认 merchant_id
-- UPDATE `product` SET `merchant_id` = 1 WHERE `merchant_id` IS NULL;

-- 为 product_category 表的现有记录设置默认 merchant_id
-- UPDATE `product_category` SET `merchant_id` = 1 WHERE `merchant_id` IS NULL;
