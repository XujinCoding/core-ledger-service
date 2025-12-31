-- 短信发送记录表
CREATE TABLE sms_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    scene VARCHAR(50) NOT NULL COMMENT '场景：MERCHANT_REGISTER/CUSTOMER_REGISTER/LOGIN/RESET_PASSWORD',
    send_status TINYINT DEFAULT 0 COMMENT '发送状态：0=待发送,1=成功,2=失败',
    response_msg VARCHAR(500) COMMENT '第三方响应信息',
    memo VARCHAR(255) COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=有效,0=无效',
    create_instant DATETIME NOT NULL COMMENT '创建时间',
    modify_instant DATETIME NOT NULL COMMENT '修改时间',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_phone (phone),
    INDEX idx_phone_scene (phone, scene),
    INDEX idx_create_instant (create_instant)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信发送记录表';
