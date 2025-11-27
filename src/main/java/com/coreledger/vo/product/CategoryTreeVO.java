package com.coreledger.vo.product;

import com.coreledger.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类树VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品分类树")
public class CategoryTreeVO {

    /**
     * 分类ID
     */
    @Schema(description = "分类ID")
    private Long id;

    /**
     * 父分类ID
     */
    @Schema(description = "父分类ID")
    private Long parentId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String name;

    /**
     * 分类层级
     */
    @Schema(description = "分类层级")
    private Integer level;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号")
    private Integer sortOrder;

    /**
     * 分类图标URL
     */
    @Schema(description = "分类图标URL")
    private String iconUrl;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Status status;

    /**
     * 子分类列表
     */
    @Schema(description = "子分类列表")
    private List<CategoryTreeVO> children = new ArrayList<>();
}
