-- =====================================================
-- Core Ledger System - Customer History Table (Snapshot)
-- Version: 1.0.0
-- Author: Core Ledger Team
-- Description: 客户历史表（快照式，保存客户表完整记录）
-- =====================================================

-- =====================================================
-- customer_history (客户历史表 - 快照模式)
-- =====================================================
CREATE TABLE `customer_history`
(
    `history_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
    `customer_id`    BIGINT       NOT NULL COMMENT '客户ID (customer.id)',
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
    `operation_type` TINYINT      NOT NULL COMMENT '操作类型: 1=创建, 2=更新, 3=删除',
    `operation_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `operator_id`    BIGINT                DEFAULT NULL COMMENT '操作人ID',
    `operator_name`  VARCHAR(50)           DEFAULT NULL COMMENT '操作人姓名',
    `create_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`history_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operation_time` (`operation_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='客户历史表（快照模式）' AUTO_INCREMENT = 10000;
