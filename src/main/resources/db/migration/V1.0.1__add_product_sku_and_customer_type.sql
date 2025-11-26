-- =====================================================
-- Core Ledger System - Product SKU & Customer Type
-- Version: 1.0.1
-- Author: Core Ledger Team
-- Description: 添加商品SPU/SKU动态属性系统 + 客户类型字段
-- =====================================================

-- =====================================================
-- 1. 修改 product 表 (增加图片、描述、位置字段)
-- =====================================================
ALTER TABLE `product`
ADD COLUMN `image_url` VARCHAR(500) COMMENT '商品主图URL' AFTER `name`,
ADD COLUMN `description` VARCHAR(500) COMMENT '商品描述' AFTER `image_url`,
ADD COLUMN `location` VARCHAR(100) COMMENT '存放位置 (如: A区3排5列)' AFTER `unit`;

-- =====================================================
-- 2. 创建商品属性模板表 (按分类预定义属性)
-- =====================================================
CREATE TABLE `product_attr_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` BIGINT NOT NULL COMMENT '关联分类ID',
    `attr_name` VARCHAR(50) NOT NULL COMMENT '属性名称 (如: 重量/规格/等级)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `is_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填: 1=必填, 0=可选',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品属性模板表';

-- =====================================================
-- 3. 创建商品属性表 (动态属性)
-- =====================================================
CREATE TABLE `product_attribute` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID (SPU)',
    `attr_name` VARCHAR(50) NOT NULL COMMENT '属性名称 (如: 重量)',
    `attr_values` VARCHAR(500) NOT NULL COMMENT '属性值列表 (JSON数组, 如: ["5斤","10斤","20斤"])',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_attr_name` (`attr_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品属性表(动态属性)';

-- =====================================================
-- 4. 创建商品SKU表 (库存单位)
-- =====================================================
CREATE TABLE `product_sku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID (SKU ID)',
    `product_id` BIGINT NOT NULL COMMENT '商品ID (SPU)',
    `attr_value_map` VARCHAR(500) NOT NULL COMMENT '属性值映射 (JSON, 如: {"重量":"5斤","等级":"一级"})',
    `sku_name` VARCHAR(150) NOT NULL COMMENT 'SKU名称 (自动生成, 如: 红富士苹果-5斤-一级)',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '销售价格',
    `cost_price` DECIMAL(10, 2) COMMENT '成本价 (可选)',
    `image_url` VARCHAR(500) COMMENT 'SKU图片URL (可选)',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表(库存单位)';

-- =====================================================
-- 5. 修改 ledger_item 表 (关联SKU)
-- =====================================================
ALTER TABLE `ledger_item`
ADD COLUMN `sku_id` BIGINT COMMENT 'SKU ID' AFTER `ledger_id`,
ADD COLUMN `sku_name` VARCHAR(150) COMMENT 'SKU名称 (冗余存储)' AFTER `product_name`,
ADD COLUMN `attr_value_map` VARCHAR(500) COMMENT '属性值映射 (冗余存储, JSON)' AFTER `sku_name`,
ADD KEY `idx_sku_id` (`sku_id`);

-- =====================================================
-- 6. 修改 customer 表 (增加客户类型字段)
-- =====================================================
ALTER TABLE `customer`
ADD COLUMN `customer_type` TINYINT NOT NULL DEFAULT 1 COMMENT '客户类型: 0=潜在客户, 1=活跃客户' AFTER `status`,
ADD KEY `idx_customer_type` (`customer_type`);

-- =====================================================
-- 7. 修改 product_category 表 (增加层级和图标字段)
-- =====================================================
ALTER TABLE `product_category`
ADD COLUMN `level` TINYINT NOT NULL DEFAULT 1 COMMENT '层级: 1-5' AFTER `name`,
ADD COLUMN `icon_url` VARCHAR(500) COMMENT '分类图标URL' AFTER `sort_order`;

-- =====================================================
-- End of Migration V1.0.1
-- =====================================================
