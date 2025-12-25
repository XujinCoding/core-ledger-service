package com.coreledger.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 客户地址更新DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "客户地址更新请求")
public class CustomerAddressUpdateDTO {

    /**
     * 关联地址ID（村级地址）
     */
    @NotNull(message = "关联地址ID不能为空")
    @Schema(description = "关联地址ID（必须为村级地址）", example = "1")
    private Long addressId;

    /**
     * 详细地址
     */
    @Size(max = 255, message = "详细地址长度不能超过255个字符 ")
    @Schema(description = "详细地址", example = "XX街道XX号")
    private String addressDetail;
}
