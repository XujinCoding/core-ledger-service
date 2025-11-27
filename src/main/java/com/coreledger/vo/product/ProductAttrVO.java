package com.coreledger.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品属性VO
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Data
@Schema(description = "商品属性")
public class ProductAttrVO {

    /**
     * 属性ID
     */
    @Schema(description = "属性ID")
    private Long id;

    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Long productId;

    /**
     * 属性名称
     */
    @Schema(description = "属性名称")
    private String attrName;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号")
    private Integer sortOrder;

    /**
     * 属性值列表
     */
    @Schema(description = "属性值列表")
    private List<ProductAttrValueVO> values = new ArrayList<>();
}
