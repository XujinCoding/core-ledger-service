-- =====================================================
-- Core Ledger System - Product Tables
-- Version: 1.0.0
-- Author: Core Ledger Team
-- Description: 商品相关表结构
-- =====================================================

-- =====================================================
-- 1. product_category (商品分类表)
-- =====================================================
CREATE TABLE `product_category`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`      BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID (0表示顶级分类)',
    `name`           VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `level`          TINYINT      NOT NULL DEFAULT 1 COMMENT '分类层级: 1-5',
    `sort_order`     INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `icon_url`       VARCHAR(500)          DEFAULT NULL COMMENT '分类图标URL',
    `memo`           VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `status`         INT          NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='商品分类表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 2. product (商品信息表)
-- =====================================================
CREATE TABLE `product`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id`    BIGINT         NOT NULL COMMENT '分类ID (product_category.id)',
    `name`           VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `image_url`      VARCHAR(500)            DEFAULT NULL COMMENT '商品主图URL',
    `description`    VARCHAR(500)            DEFAULT NULL COMMENT '商品描述',
    `price`          DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '标准价格',
    `spec`           VARCHAR(100)            DEFAULT NULL COMMENT '规格型号',
    `unit`           VARCHAR(20)    NOT NULL DEFAULT '件' COMMENT '单位 (件/箱/斤/公斤等)',
    `location`       VARCHAR(100)            DEFAULT NULL COMMENT '存放位置 (如: A区3排5列)',
    `memo`           VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `status`         INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='商品信息表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 3. product_attr (商品属性表)
-- =====================================================
CREATE TABLE `product_attr`
(
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id`     BIGINT      NOT NULL COMMENT '商品ID (product.id)',
    `attr_name`      VARCHAR(50) NOT NULL COMMENT '属性名称 (如: 重量, 颜色)',
    `sort_order`     INT         NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo`           VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    `status`         INT         NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_attr_name` (`attr_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='商品属性表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 4. product_attr_value (商品属性值表)
-- =====================================================
CREATE TABLE `product_attr_value`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_attr_id`   BIGINT       NOT NULL COMMENT '商品属性ID (product_attr.id)',
    `value`             VARCHAR(255) NOT NULL COMMENT '属性值',
    `sort_order`        INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo`              VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `status`            INT          NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`           INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_product_attr_id` (`product_attr_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='商品属性值表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 5. product_sku (商品SKU表)
-- =====================================================
CREATE TABLE `product_sku`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id`     BIGINT         NOT NULL COMMENT '商品ID (product.id)',
    `sku_name`       VARCHAR(150)   NOT NULL COMMENT 'SKU名称 (自动生成, 如: 红富士苹果-5斤-一级)',
    `price_status`   TINYINT        NOT NULL DEFAULT 0 COMMENT '定价状态: 0=未定价, 1=已定价',
    `price`          DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'SKU销售价格',
    `image_url`      VARCHAR(500)            DEFAULT NULL COMMENT 'SKU图片URL',
    `sort_order`     INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo`           VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `status`         INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_sku_name` (`sku_name`),
    KEY `idx_price` (`price`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='商品SKU表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 6. product_sku_attr (商品SKU属性值表)
-- =====================================================
CREATE TABLE `product_sku_attr`
(
    `id`                       BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_id`                   BIGINT         NOT NULL COMMENT 'SKU ID (product_sku.id)',
    `product_attr_id`          BIGINT         NOT NULL COMMENT '商品属性ID (product_attr.id)',
    `product_attr_name`        VARCHAR(50)    NOT NULL COMMENT '商品属性名称 (冗余)',
    `product_attr_value_id`    BIGINT         NOT NULL COMMENT '商品属性值ID (product_attr_value.id)',
    `product_attr_value_name`  VARCHAR(255)   NOT NULL COMMENT '商品属性值名称 (冗余)',
    `sort_order`               INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    `memo`                     VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `status`                   INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant`           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant`           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`                  INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_product_attr_id` (`product_attr_id`),
    KEY `idx_product_attr_value_id` (`product_attr_value_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='商品SKU属性值表' AUTO_INCREMENT = 10000;
