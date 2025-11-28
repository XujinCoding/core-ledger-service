package com.coreledger.vo.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 地址链VO（用于回显地址级联选择器）
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "地址链VO")
public class AddressChainVO {

    /**
     * 地址ID数组（从省到当前地址的完整路径）
     */
    @Schema(description = "地址ID数组（从省到当前地址）", example = "[1, 101, 1001, 10001, 100001]")
    private List<Long> addressIds;

    /**
     * 地址名称数组（从省到当前地址的完整路径）
     */
    @Schema(description = "地址名称数组", example = "[\"广东省\", \"深圳市\", \"南山区\", \"西丽街道\", \"留仙村\"]")
    private List<String> addressNames;

    /**
     * 地址层级数组
     */
    @Schema(description = "地址层级数组", example = "[1, 2, 3, 4, 5]")
    private List<Integer> addressLevels;

    /**
     * 完整地址路径
     */
    @Schema(description = "完整地址路径", example = "广东省-深圳市-南山区-西丽街道-留仙村")
    private String fullPath;

    /**
     * 最终地址ID（客户保存的地址ID）
     */
    @Schema(description = "最终地址ID", example = "100001")
    private Long targetAddressId;

    /**
     * 最终地址名称
     */
    @Schema(description = "最终地址名称", example = "留仙村")
    private String targetAddressName;
}
