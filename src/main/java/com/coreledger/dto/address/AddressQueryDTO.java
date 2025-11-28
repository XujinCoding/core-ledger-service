package com.coreledger.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 地址查询请求DTO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "地址查询请求DTO")
public class AddressQueryDTO {

    /**
     * 父级ID（查询子级地址时使用，0或null表示顶级）
     */
    @Schema(description = "父级ID（0或null表示查询省份）", example = "0")
    private Long parentId;

    /**
     * 地址层级（1=省, 2=市, 3=区县, 4=镇, 5=村）
     */
    @Schema(description = "地址层级（1=省, 2=市, 3=区县, 4=镇, 5=村）", example = "1")
    private Integer level;
}
