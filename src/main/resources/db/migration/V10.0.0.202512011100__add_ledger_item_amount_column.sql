-- =====================================================
-- 为账本明细表添加小计金额字段
-- =====================================================

-- 添加 amount 字段
ALTER TABLE `ledger_item`
    ADD COLUMN `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '小计金额（price × quantity）' AFTER `quantity`;

-- 回填历史数据（如果有）
UPDATE `ledger_item`
SET `amount` = `price` * `quantity`
WHERE `amount` = 0.00;

-- 添加复合索引（优化根据账本ID和状态查询）
ALTER TABLE `ledger_item`
    ADD KEY `idx_ledger_id_status` (`ledger_id`, `status`);
