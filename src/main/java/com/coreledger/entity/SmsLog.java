package com.coreledger.entity;

import com.coreledger.config.converter.SmsSceneConverter;
import com.coreledger.config.converter.SmsSendStatusConverter;
import com.coreledger.enums.SmsSendStatus;
import com.coreledger.enums.SmsScene;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 短信发送记录实体
 */
@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "sms_log", indexes = {
        @Index(name = "idx_phone", columnList = "phone"),
        @Index(name = "idx_phone_scene", columnList = "phone, scene"),
        @Index(name = "idx_create_instant", columnList = "create_instant")
})
public class SmsLog extends BaseEntity {

    /**
     * 手机号
     */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * 验证码
     */
    @Column(name = "code", nullable = false, length = 10)
    private String code;

    /**
     * 场景
     */
    @Column(name = "scene", nullable = false, length = 50)
    @Convert(converter = SmsSceneConverter.class)
    private SmsScene scene;

    /**
     * 发送状态
     */
    @Column(name = "send_status")
    @Convert(converter = SmsSendStatusConverter.class)
    private SmsSendStatus sendStatus = SmsSendStatus.PENDING;

    /**
     * 第三方响应信息
     */
    @Column(name = "response_msg", length = 500)
    private String responseMsg;
}
