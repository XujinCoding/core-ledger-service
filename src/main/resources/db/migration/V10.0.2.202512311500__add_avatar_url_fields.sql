-- 商户表增加头像字段
ALTER TABLE merchant ADD COLUMN avatar_url VARCHAR(500) COMMENT '商户头像/Logo URL' AFTER qr_code_url;

-- 客户表增加头像字段
ALTER TABLE customer ADD COLUMN avatar_url VARCHAR(500) COMMENT '客户头像URL' AFTER address_detail;
