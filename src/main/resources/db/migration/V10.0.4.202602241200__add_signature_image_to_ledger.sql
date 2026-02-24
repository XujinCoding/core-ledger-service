-- 添加签名图片字段到账单表
-- @author Core Ledger Team
-- @since 1.0.0

ALTER TABLE ledger ADD COLUMN signature_image_url VARCHAR(500) COMMENT '签名图片URL';

-- 添加索引以提高查询性能（可选）
CREATE INDEX idx_ledger_signature ON ledger(signature_image_url);
