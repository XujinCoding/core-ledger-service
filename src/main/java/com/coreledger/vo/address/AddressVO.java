package com.coreledger.vo.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 地址信息VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "地址信息VO")
public class AddressVO {

    /**
     * 地址ID
     */
    @Schema(description = "地址ID", example = "1")
    private Long id;

    /**
     * 父级ID
     */
    @Schema(description = "父级ID（0表示顶级）", example = "0")
    private Long parentId;

    /**
     * 地址名称
     */
    @Schema(description = "地址名称", example = "广东省")
    private String name;

    /**
     * 地址层级
     */
    @Schema(description = "地址层级（1=省, 2=市, 3=区县, 4=镇, 5=村）", example = "1")
    private Integer level;

    /**
     * 地址层级描述
     */
    @Schema(description = "地址层级描述", example = "省")
    private String levelDesc;

    /**
     * 全称路径
     */
    @Schema(description = "全称路径", example = "广东省-深圳市-南山区-西丽街道-留仙村")
    private String mergerName;

    /**
     * 是否为顶级
     */
    @Schema(description = "是否为顶级", example = "true")
    private Boolean isTopLevel;

    /**
     * 是否为村级
     */
    @Schema(description = "是否为村级及以上（可用于客户地址）", example = "false")
    private Boolean isVillageLevel;
}
