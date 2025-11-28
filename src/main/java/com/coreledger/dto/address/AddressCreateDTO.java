package com.coreledger.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 地址创建请求DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "地址创建请求DTO")
public class AddressCreateDTO {

    /**
     * 父级ID（0或null表示创建顶级地址-省）
     */
    @Schema(description = "父级ID（0或null表示创建省级地址）", example = "1")
    private Long parentId;

    /**
     * 地址名称
     */
    @NotBlank(message = "地址名称不能为空")
    @Size(max = 100, message = "地址名称不能超过100个字符")
    @Schema(description = "地址名称", example = "深圳市", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
