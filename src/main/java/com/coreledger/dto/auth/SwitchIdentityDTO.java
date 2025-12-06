package com.coreledger.dto.auth;

import com.coreledger.enums.IdentityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 切换身份DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "切换身份请求")
public class SwitchIdentityDTO {

    /**
     * 身份类型：MERCHANT_OWNER 或 CUSTOMER
     */
    @NotNull(message = "身份类型不能为空")
    @Schema(description = "身份类型", example = "MERCHANT_OWNER")
    private IdentityType identityType;

    /**
     * 商户ID（当role=MERCHANT时必填）
     */
    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    /**
     * 客户ID（当role=CUSTOMER时必填）
     */
    @Schema(description = "客户ID", example = "1")
    private Long customerId;

    /**
     * 用户Token
     */
    @NotNull(message = "Token不能为空")
    @Schema(description = "用户Token", example = "eyJhbGc...")
    private String token;
}
