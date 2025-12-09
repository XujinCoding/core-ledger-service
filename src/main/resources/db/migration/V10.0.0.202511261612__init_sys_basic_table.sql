-- =====================================================
-- Core Ledger System - Complete Database Schema
-- Version: 2.0.0
-- Author: Core Ledger Team
-- Description: 核芯账本系统完整数据库表结构（合并版）
-- =====================================================

-- =====================================================
-- 1. sys_user (用户表)
-- =====================================================
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名（可选，管理员必填）',
    `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(BCrypt加密，管理员必填)',
    `wx_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID（唯一标识）',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
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
-- 3. merchant (商户表)
-- =====================================================
CREATE TABLE `merchant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(32) NOT NULL COMMENT '商户编号，格式：M_yyyyMMddHHmmss_随机3位',
    `name` VARCHAR(100) NOT NULL COMMENT '商户名称',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `address_id` BIGINT NOT NULL COMMENT '关联地址ID (sys_address.id)',
    `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址 (门牌号等)',
    `owner_user_id` BIGINT NOT NULL COMMENT '商户所有者User ID',
    `invite_code` VARCHAR(20) NOT NULL COMMENT '邀请码，用于生成二维码',
    `qr_code_url` VARCHAR(500) DEFAULT NULL COMMENT '二维码URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_code` (`code`),
     UNIQUE KEY `uk_invite_code` (`invite_code`),
     KEY `idx_owner_user_id` (`owner_user_id`),
     CONSTRAINT `fk_merchant_owner_user_id` FOREIGN KEY (`owner_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商户表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 4. customer (客户信息表)
-- =====================================================
CREATE TABLE `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(32) NOT NULL COMMENT '客户编号，格式：C_yyyyMMddHHmmss_随机3位',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联的User ID，允许为空（商户手动创建时）',
    `merchant_id` BIGINT DEFAULT NULL COMMENT '所属商户ID',
    `is_registered` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已注册: 0=未注册, 1=已注册',
    `name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `alias` VARCHAR(50) DEFAULT NULL COMMENT '别名/昵称',
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别: 0=未知, 1=男, 2=女',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `address_id` BIGINT NOT NULL COMMENT '关联地址ID (sys_address.id)',
    `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址 (门牌号等)',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `customer_type` TINYINT NOT NULL DEFAULT 1 COMMENT '客户类型: 0=潜在客户, 1=活跃客户',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_user_merchant` (`user_id`, `merchant_id`),
     UNIQUE KEY `uk_code_merchant` (`code`, `merchant_id`),
     KEY `idx_name` (`name`),
     KEY `idx_address_id` (`address_id`),
     KEY `idx_customer_type` (`customer_type`),
     KEY `idx_merchant_id` (`merchant_id`),
     KEY `idx_user_id` (`user_id`),
     KEY `idx_phone` (`phone`),
     CONSTRAINT `fk_customer_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
     CONSTRAINT `fk_customer_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户信息表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 5. customer_history (客户历史表 - 快照模式)
-- =====================================================
CREATE TABLE `customer_history` (
    `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID (customer.id)',
    `name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `alias` VARCHAR(50) DEFAULT NULL COMMENT '别名/昵称',
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别: 0=未知, 1=男, 2=女',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `address_id` BIGINT NOT NULL COMMENT '关联地址ID (sys_address.id)',
    `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址 (门牌号等)',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `customer_type` TINYINT NOT NULL DEFAULT 1 COMMENT '客户类型: 0=潜在客户, 1=活跃客户',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `operation_type` TINYINT NOT NULL COMMENT '操作类型: 1=创建, 2=更新, 3=删除',
    `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
     PRIMARY KEY (`history_id`),
     KEY `idx_customer_id` (`customer_id`),
     KEY `idx_operation_type` (`operation_type`),
     KEY `idx_operation_time` (`operation_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户历史表（快照模式）' AUTO_INCREMENT = 10000;

-- =====================================================
-- 6. product_category (商品分类表)
-- =====================================================
CREATE TABLE `product_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT NOT NULL COMMENT '所属商户ID，用于数据隔离',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID (0表示顶级分类)',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `level` TINYINT NOT NULL DEFAULT 1 COMMENT '分类层级: 1-5',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '分类图标URL',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     KEY `idx_parent_id` (`parent_id`),
     KEY `idx_merchant_id` (`merchant_id`),
     CONSTRAINT `fk_product_category_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 7. product (商品信息表)
-- =====================================================
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT NOT NULL COMMENT '所属商户ID，用于数据隔离',
    `category_id` BIGINT NOT NULL COMMENT '分类ID (product_category.id)',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '商品主图URL',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '标准价格',
    `spec` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `unit` VARCHAR(20) NOT NULL DEFAULT '件' COMMENT '单位 (件/箱/斤/公斤等)',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '存放位置 (如: A区3排5列)',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     KEY `idx_category_id` (`category_id`),
     KEY `idx_name` (`name`),
     KEY `idx_merchant_id` (`merchant_id`),
     CONSTRAINT `fk_product_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品信息表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 8. product_attr (商品属性表)
-- =====================================================
CREATE TABLE `product_attr` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID (product.id)',
    `attr_name` VARCHAR(50) NOT NULL COMMENT '属性名称 (如: 重量, 颜色)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     KEY `idx_product_id` (`product_id`),
     KEY `idx_attr_name` (`attr_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品属性表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 9. product_attr_value (商品属性值表)
-- =====================================================
CREATE TABLE `product_attr_value` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_attr_id` BIGINT NOT NULL COMMENT '商品属性ID (product_attr.id)',
    `value` VARCHAR(255) NOT NULL COMMENT '属性值',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     KEY `idx_product_attr_id` (`product_attr_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品属性值表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 10. product_sku (商品SKU表)
-- =====================================================
CREATE TABLE `product_sku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID (product.id)',
    `sku_name` VARCHAR(150) NOT NULL COMMENT 'SKU名称 (自动生成, 如: 红富士苹果-5斤-一级)',
    `price_status` TINYINT NOT NULL DEFAULT 0 COMMENT '定价状态: 0=未定价, 1=已定价',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'SKU销售价格',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT 'SKU图片URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     KEY `idx_product_id` (`product_id`),
     KEY `idx_sku_name` (`sku_name`),
     KEY `idx_price` (`price`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品SKU表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 11. product_sku_attr (商品SKU属性值表)
-- =====================================================
CREATE TABLE `product_sku_attr` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID (product_sku.id)',
    `product_attr_id` BIGINT NOT NULL COMMENT '商品属性ID (product_attr.id)',
    `product_attr_name` VARCHAR(50) NOT NULL COMMENT '商品属性名称 (冗余)',
    `product_attr_value_id` BIGINT NOT NULL COMMENT '商品属性值ID (product_attr_value.id)',
    `product_attr_value_name` VARCHAR(255) NOT NULL COMMENT '商品属性值名称 (冗余)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     KEY `idx_sku_id` (`sku_id`),
     KEY `idx_product_attr_id` (`product_attr_id`),
     KEY `idx_product_attr_value_id` (`product_attr_value_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品SKU属性值表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 12. ledger (账本主表)
-- =====================================================
CREATE TABLE `ledger` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT NOT NULL COMMENT '所属商户ID，用于数据隔离',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID (customer.id)',
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
     KEY `idx_create_instant` (`create_instant`),
     KEY `idx_merchant_id` (`merchant_id`),
     CONSTRAINT `fk_ledger_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账本主表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 13. ledger_item (账本明细表)
-- =====================================================
CREATE TABLE `ledger_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ledger_id` BIGINT NOT NULL COMMENT '账本ID (ledger.id)',
    `product_id` BIGINT NOT NULL COMMENT '商品ID (product.id)',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称 (冗余)',
    `sku_id` BIGINT DEFAULT NULL COMMENT 'SKU ID (product_sku.id)',
    `sku_name` VARCHAR(150) DEFAULT NULL COMMENT 'SKU名称 (冗余)',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '实际售价 (单价)',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '小计金额（price × quantity）',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=已删除',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     KEY `idx_ledger_id` (`ledger_id`),
     KEY `idx_product_id` (`product_id`),
     KEY `idx_sku_id` (`sku_id`),
     KEY `idx_ledger_id_status` (`ledger_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账本明细表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 14. payment_record (支付流水表)
-- =====================================================
CREATE TABLE `payment_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ledger_id` BIGINT NOT NULL COMMENT '账本ID (ledger.id)',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_method` TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式: 1=现金, 2=微信, 3=支付宝, 4=银行转账',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=已删除',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`),
     KEY `idx_ledger_id` (`ledger_id`),
     KEY `idx_payment_method` (`payment_method`),
     KEY `idx_create_instant` (`create_instant`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付流水表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 初始化默认管理员用户
-- 用户名: admin
-- 密码: admin123 (BCrypt加密)
-- 角色: 1 (管理员)
-- =====================================================
INSERT INTO `sys_user` (`username`, `password`, `memo`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统默认管理员');

-- =====================================================
-- 脚本执行完成
-- =====================================================