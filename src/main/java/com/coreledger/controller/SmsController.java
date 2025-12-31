package com.coreledger.controller;

import com.coreledger.common.Result;
import com.coreledger.dto.sms.SendSmsDTO;
import com.coreledger.dto.sms.VerifySmsDTO;
import com.coreledger.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 短信验证码控制器
 */
@Tag(name = "短信验证码", description = "短信验证码发送和验证接口")
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码", description = "发送短信验证码，60秒内不可重复发送，每日上限10条")
    @PostMapping("/send")
    public Result<Void> sendSmsCode(@Valid @RequestBody SendSmsDTO dto) {
        smsService.sendSmsCode(dto.getPhone(), dto.getScene());
        return Result.success("验证码发送成功");
    }

    /**
     * 验证短信验证码
     */
    @Operation(summary = "验证短信验证码", description = "验证短信验证码，验证成功后验证码失效")
    @PostMapping("/verify")
    public Result<Boolean> verifySmsCode(@Valid @RequestBody VerifySmsDTO dto) {
        boolean result = smsService.verifySmsCode(dto.getPhone(), dto.getCode(), dto.getScene());
        return Result.success(result);
    }
}
