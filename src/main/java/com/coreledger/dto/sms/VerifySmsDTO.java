package com.coreledger.dto.sms;

import com.coreledger.enums.SmsScene;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 验证短信验证码请求DTO
 */
@Data
@Schema(description = "验证短信验证码请求")
public class VerifySmsDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true, example = "13800138000")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4,6}$", message = "验证码格式不正确")
    @Schema(description = "验证码", required = true, example = "123456")
    private String code;

    @NotNull(message = "场景不能为空")
    @Schema(description = "场景", required = true, example = "MERCHANT_REGISTER")
    private SmsScene scene;
}
