package com.coreledger.dto.sms;

import com.coreledger.enums.SmsScene;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送短信验证码请求DTO
 */
@Data
@Schema(description = "发送短信验证码请求")
public class SendSmsDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true, example = "13800138000")
    private String phone;

    @NotNull(message = "场景不能为空")
    @Schema(description = "场景：MERCHANT_REGISTER=商户注册, CUSTOMER_REGISTER=客户注册, LOGIN=登录验证, RESET_PASSWORD=重置密码",
            required = true, example = "MERCHANT_REGISTER")
    private SmsScene scene;
}
