-- =====================================================
-- Core Ledger System - Customer and Ledger Tables
-- Version: 1.0.0
-- Author: Core Ledger Team
-- Description: 客户和账本相关表结构
-- =====================================================

-- =====================================================
-- 1. customer (客户信息表)
-- =====================================================
CREATE TABLE `customer`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`           VARCHAR(50)  NOT NULL COMMENT '客户姓名',
    `phone`          VARCHAR(20)  NOT NULL COMMENT '手机号',
    `alias`          VARCHAR(50)           DEFAULT NULL COMMENT '别名/昵称',
    `gender`         TINYINT      NOT NULL DEFAULT 0 COMMENT '性别: 0=未知, 1=男, 2=女',
    `age`            INT                   DEFAULT NULL COMMENT '年龄',
    `address_id`     BIGINT       NOT NULL COMMENT '关联地址ID (sys_address.id)',
    `address_detail` VARCHAR(255)          DEFAULT NULL COMMENT '详细地址 (门牌号等)',
    `memo`           VARCHAR(255)          DEFAULT NULL COMMENT '备注',
    `customer_type`  TINYINT      NOT NULL DEFAULT 1 COMMENT '客户类型: 0=潜在客户, 1=活跃客户',
    `status`         INT          NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_name` (`name`),
    KEY `idx_address_id` (`address_id`),
    KEY `idx_customer_type` (`customer_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='客户信息表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 2. ledger (账本主表)
-- =====================================================
CREATE TABLE `ledger`
(
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id`     BIGINT         NOT NULL COMMENT '客户ID (customer.id)',
    `total_amount`    DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '应收总金额',
    `paid_amount`     DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实收金额',
    `discount_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '抹零/优惠金额',
    `ledger_status`   TINYINT        NOT NULL DEFAULT 1 COMMENT '账本状态: 1=进行中, 2=部分缴费, 3=已结清, 4=赊账中, 5=已关闭',
    `memo`            VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `status`          INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `create_instant`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`         INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_ledger_status` (`ledger_status`),
    KEY `idx_create_instant` (`create_instant`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='账本主表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 3. ledger_item (账本明细表)
-- =====================================================
CREATE TABLE `ledger_item`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ledger_id`      BIGINT         NOT NULL COMMENT '账本ID (ledger.id)',
    `product_id`     BIGINT         NOT NULL COMMENT '商品ID (product.id)',
    `product_name`   VARCHAR(100)   NOT NULL COMMENT '商品名称 (冗余)',
    `sku_id`         BIGINT                  DEFAULT NULL COMMENT 'SKU ID (product_sku.id)',
    `sku_name`       VARCHAR(150)            DEFAULT NULL COMMENT 'SKU名称 (冗余)',
    `price`          DECIMAL(10, 2) NOT NULL COMMENT '实际售价 (单价)',
    `quantity`       INT            NOT NULL DEFAULT 1 COMMENT '数量',
    `memo`           VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `status`         INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=已删除',
    `create_instant` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_ledger_id` (`ledger_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='账本明细表' AUTO_INCREMENT = 10000;

-- =====================================================
-- 4. payment_record (支付流水表)
-- =====================================================
CREATE TABLE `payment_record`
(
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ledger_id`      BIGINT         NOT NULL COMMENT '账本ID (ledger.id)',
    `amount`         DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_method` TINYINT        NOT NULL DEFAULT 1 COMMENT '支付方式: 1=现金, 2=微信, 3=支付宝, 4=银行转账',
    `memo`           VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `status`         INT            NOT NULL DEFAULT 1 COMMENT '状态: 1=有效, 0=已删除',
    `create_instant` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    `modify_instant` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT            NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_ledger_id` (`ledger_id`),
    KEY `idx_payment_method` (`payment_method`),
    KEY `idx_create_instant` (`create_instant`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='支付流水表' AUTO_INCREMENT = 10000;
