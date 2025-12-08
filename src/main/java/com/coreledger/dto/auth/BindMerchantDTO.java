package com.coreledger.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定商户DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户扫码绑定商户请求")
public class BindMerchantDTO {

    /**
     * 商户邀请码
     */
    @NotBlank(message = "邀请码不能为空")
    @Schema(description = "商户邀请码", example = "ABC123")
    private String inviteCode;
}
