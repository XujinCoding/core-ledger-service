-- =====================================================
-- 核芯账本系统 - 多租户数据隔离架构升级SQL
-- Version: 2.0.0
-- Description: 数据库表结构变更脚本
-- =====================================================

-- =====================================================
-- 1. 扩展 sys_user 表（用户个人主档案）
-- =====================================================
ALTER TABLE `sys_user`
    ADD COLUMN `name`           VARCHAR(100) DEFAULT NULL COMMENT '真实姓名' AFTER `wx_openid`,
    ADD COLUMN `nickname`       VARCHAR(100) DEFAULT NULL COMMENT '昵称' AFTER `name`,
    ADD COLUMN `avatar_url`     VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL' AFTER `nickname`,
    ADD COLUMN `gender`         TINYINT      DEFAULT 0 COMMENT '性别: 0=未知, 1=男, 2=女' AFTER `avatar_url`,
    ADD COLUMN `age`            INT          DEFAULT NULL COMMENT '年龄' AFTER `gender`,
    ADD COLUMN `phone`          VARCHAR(20)  DEFAULT NULL COMMENT '手机号' AFTER `age`,
    ADD COLUMN `address_id`     BIGINT       DEFAULT NULL COMMENT '关联地址ID (sys_address.id)' AFTER `phone`,
    ADD COLUMN `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址 (门牌号等)' AFTER `address_id`;

-- 添加索引
ALTER TABLE `sys_user`
    ADD UNIQUE KEY `uk_phone` (phone);

-- =====================================================
-- 2. 新增 user_merchant_relation 表（用户-商家绑定关系）
-- =====================================================
CREATE TABLE `user_merchant_relation`
(
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        BIGINT   NOT NULL COMMENT '系统用户ID',
    `merchant_id`    BIGINT   NOT NULL COMMENT '商家ID',
    `identity`       TINYINT  NOT NULL DEFAULT 2 COMMENT '角色: 0=商户老板, 1=员工, 2=客户',
    -- 绑定时间
    `bind_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    -- 状态
    `status`         TINYINT  NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `memo`           VARCHAR(255)      DEFAULT NULL COMMENT '备注',
    `create_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_instant` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version`        INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_merchant_role` (`user_id`, `merchant_id`, `identity`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_identity` (`identity`),
    KEY `idx_status` (`status`),

    CONSTRAINT `fk_relation_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_relation_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户-商家绑定关系表'
  AUTO_INCREMENT = 10000;

-- =====================================================
-- 3. 扩展 customer 表（商家维度的客户业务档案）
-- =====================================================

-- 新增字段
ALTER TABLE `customer`
    ADD COLUMN `points` INT DEFAULT 0 COMMENT '积分';

-- 注意: customer_type字段暂时保留，待代码迁移完成后再删除
-- ALTER TABLE `customer` DROP COLUMN customer_type;