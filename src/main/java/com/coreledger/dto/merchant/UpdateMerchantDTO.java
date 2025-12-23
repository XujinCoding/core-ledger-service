package com.coreledger.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新商户信息DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "更新商户信息请求")
public class UpdateMerchantDTO {

    @Schema(description = "商户名称")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "地址ID")
    private Long addressId;

    @Schema(description = "详细地址")
    private String addressDetail;
}
